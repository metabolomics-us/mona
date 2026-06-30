package edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound.classyfire

import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.MonaMapper
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{ClassificationCache, Compound, MetaData, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.api.ClassificationCacheRestClient
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound.CompoundProcessor
import edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound.cts.CTSLiteService
import edu.ucdavis.fiehnlab.mona.backend.curation.util.CommonMetaData
import org.springframework.batch.item.ItemProcessor
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.http.{HttpHeaders, HttpStatus, ResponseEntity}
import org.springframework.web.client.{HttpStatusCodeException, ResourceAccessException, RestClientException, RestOperations}

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicBoolean
import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, Buffer}
import scala.jdk.CollectionConverters._
import scala.util.Try

/**
  * Connects to the external ClassyFire service and classifies the compounds of a spectrum. This processor no
  * longer runs inside the main curation workflow. It is driven by the dedicated single consumer ClassyFire
  * queue so classification cannot bottleneck curation, and so all outbound traffic is serialized through one
  * thread that paces requests and backs off on rate limits.
  *
  * Behavior:
  *   - skips a compound that already has classification data
  *   - only calls ClassyFire when the compound has a syntactically valid InChIKey
  *   - dedupes by the 14 character InChIKey first block (the connectivity skeleton) via a persistent cache, so
  *     each skeleton is sent to ClassyFire at most once and reused for every spectrum sharing it
  *   - on a 404 schedules an async query and records its id so the listener re-enqueues the spectrum to poll
  *   - paces requests and retries on HTTP 429, and infers reachability from actual responses
  */
@Step(description = "run's classification rules from the wishart's lab classyfire tool")
class ClassyfireProcessor extends ItemProcessor[Spectrum, Spectrum] with LazyLogging {

  @Autowired
  val compoundProcessor: CompoundProcessor = null

  @Autowired
  protected val restOperations: RestOperations = null

  // Optional so the curation scheduler, which also instantiates this component scanned processor but never
  // runs it and does not wire the persistence client, still starts. The runner always has it
  @Autowired(required = false)
  protected val classificationCacheRestClient: ClassificationCacheRestClient = null

  @Autowired
  protected val classyfireStats: ClassyfireStats = null

  // Null safe stat update, the stats bean is always present in production but may be absent in unit tests
  private def stat(update: ClassyfireStats => Unit): Unit = if (classyfireStats != null) update(classyfireStats)

  // Minimum spacing between outbound ClassyFire requests, the floor the adaptive spacing decays back to
  @Value("${mona.classyfire.request.spacing:2500}")
  val requestSpacingMs: Long = 2500

  // Upper bound the adaptive spacing is allowed to widen to after repeated 429s
  @Value("${mona.classyfire.request.spacing.max:15000}")
  val maxSpacingMs: Long = 15000

  // Factor the spacing is multiplied by on each 429, widening it toward ClassyFire's real limit
  @Value("${mona.classyfire.request.spacing.increase:1.5}")
  val spacingIncreaseFactor: Double = 1.5

  // Number of consecutive successful requests before the spacing decays one step back toward the floor
  @Value("${mona.classyfire.request.spacing.recovery:5}")
  val spacingRecoveryThreshold: Int = 5

  // Milliseconds the spacing is reduced by on each recovery step
  @Value("${mona.classyfire.request.spacing.decay:250}")
  val spacingDecayStepMs: Long = 250

  // How many times a single request is retried after an HTTP 429 before giving up
  @Value("${mona.classyfire.retry.max:5}")
  val maxRetries: Int = 5

  // Base backoff after an HTTP 429, doubled on each subsequent 429
  @Value("${mona.classyfire.retry.backoff:6000}")
  val backoffBaseMs: Long = 6000

  // How long to fast fail after ClassyFire was found unreachable before probing it again
  @Value("${mona.classyfire.down.cooldown:60000}")
  val downCooldownMs: Long = 60000

  private val objectMapper: ObjectMapper = MonaMapper.create

  // Reachability is inferred from real responses rather than an ICMP probe
  private val serviceUp: AtomicBoolean = new AtomicBoolean(true)
  @volatile private var downSince: Long = 0

  // Pacing state, guarded so concurrent consumers (if ever configured) still serialize their spacing.
  // currentSpacingMs adapts between requestSpacingMs (floor) and maxSpacingMs (cap): it widens on a 429 and
  // decays back toward the floor after a run of successes, so the steady state converges near ClassyFire's
  // real per IP limit rather than wasting a round trip on a 429 every other request
  private val pacingLock = new Object
  private var lastRequestTime: Long = 0
  private var currentSpacingMs: Long = requestSpacingMs
  private var consecutiveSuccesses: Int = 0

  /**
    * Classify every compound of the spectrum. Compounds are mutated in place
    *
    * @param spectrum
    * @return
    */
  override def process(spectrum: Spectrum): Spectrum = {
    logger.info(s"${spectrum.getId}: Retrieving classification data from ClassyFire")

    val compounds = spectrum.getCompound.asScala.map { compound =>
      if (compound.getClassification == null) {
        compound.setClassification(Buffer[MetaData]().asJava)
      }
      classify(compound, spectrum.getId)
    }

    spectrum.setCompound(compounds.asJava)
    spectrum
  }

  /**
    * True if any compound still carries a scheduled ClassyFire query id, meaning the spectrum should be
    * re-enqueued so the async classification can be polled again
    *
    * @param spectrum
    * @return
    */
  def hasPendingClassification(spectrum: Spectrum): Boolean = {
    spectrum.getCompound != null && spectrum.getCompound.asScala.exists { compound =>
      compound.getClassification != null &&
        compound.getClassification.asScala.exists(_.getName == CommonMetaData.CLASSYFIRE_QUERY_ID)
    }
  }

  /**
    * Add a transient marker indicating ClassyFire could not be reached so the frontend can show that
    * classification is pending rather than genuinely empty. The marker is computed metadata, so it is
    * stripped by RemoveComputedData at the start of the next curation run and re-added only if it fails again
    *
    * @param compound
    * @param id
    * @return
    */
  def markClassyfireUnavailable(compound: Compound, id: String): Compound = {
    logger.warn(s"$id: Flagging compound classification as pending, ClassyFire was unavailable")

    val alreadyFlagged: Boolean = compound.getMetaData != null &&
      compound.getMetaData.asScala.exists(_.getName == CommonMetaData.CLASSYFIRE_STATUS)

    if (!alreadyFlagged) {
      val metaData = new MetaData(null, CommonMetaData.CLASSYFIRE_STATUS, CommonMetaData.CLASSYFIRE_STATUS_UNAVAILABLE, true, "classification", true, null)
      val existing: Buffer[MetaData] = if (compound.getMetaData != null) compound.getMetaData.asScala else Buffer[MetaData]()
      compound.setMetaData((existing :+ metaData).asJava)
    }

    compound
  }

  /**
    * Resolve the InChIKey to classify, preferring a computed value over the submitted one
    *
    * @param compound
    * @return
    */
  def resolveInchiKey(compound: Compound): String = {
    if (compound.getMetaData != null && compound.getMetaData.asScala.exists(x => x.getName == CommonMetaData.INCHI_KEY && x.getComputed)) {
      compound.getMetaData.asScala.filter(x => x.getName == CommonMetaData.INCHI_KEY && x.getComputed).map(_.getValue.toString).headOption.orNull
    } else {
      compound.getInchiKey
    }
  }

  /**
    * A usable InChIKey is non blank and matches the standard layout. Reuses the pattern from CTSLiteService
    *
    * @param inchiKey
    * @return
    */
  def isValidInchiKey(inchiKey: String): Boolean = {
    inchiKey != null && inchiKey.trim.nonEmpty && inchiKey.matches(CTSLiteService.INCHIKEY_PATTERN)
  }

  /**
    * Classify a single compound, cache first
    *
    * @param compound
    * @param id
    * @return
    */
  def classify(compound: Compound, id: String): Compound = {
    val classyfireQueryId: Buffer[MetaData] = compound.getClassification.asScala.filter(_.getName == CommonMetaData.CLASSYFIRE_QUERY_ID)
    val alreadyClassified: Boolean = compound.getClassification.asScala.exists(_.getName != CommonMetaData.CLASSYFIRE_QUERY_ID)
    val inchiKey: String = resolveInchiKey(compound)

    logger.info(s"$id: Checking ClassyFire classification for InChIKey ${if (inchiKey == null || inchiKey.trim.isEmpty) "(none)" else inchiKey}")

    if (alreadyClassified) {
      logger.info(s"$id: Already have ClassyFire data, skipping...")
      stat(_.incAlreadyClassified())
      // Seed the skeleton cache from this existing classification (from an earlier run, possibly the old
      // inline runner) so other spectra sharing the skeleton can reuse it without calling ClassyFire
      warmCacheFromExisting(compound, id)
      compound
    }

    // Poll a previously scheduled async query
    else if (classyfireQueryId.nonEmpty) {
      pollScheduledQuery(compound, id, classyfireQueryId.head.getValue.toString)
    }

    // Only call ClassyFire when we have a valid InChIKey to look up
    else {
      if (!isValidInchiKey(inchiKey)) {
        logger.info(s"$id: No valid InChIKey, skipping ClassyFire classification")
        stat(_.incMissingInchiKey())
        compound
      } else {
        val block: String = inchiKey.take(14)

        classificationCacheLookup(block) match {
          case Some(cache) =>
            logger.info(s"$id: Reusing cached classification for skeleton $block")
            stat(_.incDbCacheHit())
            applyCachedClassification(compound, cache)
          case None =>
            lookupEntities(compound, id, inchiKey)
        }
      }
    }
  }

  /**
    * Poll a scheduled query for its result. If it is not done yet the query id is left in place so the
    * listener re-enqueues the spectrum to poll again later
    *
    * @param compound
    * @param id
    * @param queryId
    * @return
    */
  def pollScheduledQuery(compound: Compound, id: String, queryId: String): Compound = {
    if (circuitOpen) {
      return markClassyfireUnavailable(compound, id)
    }

    val url = s"http://classyfire.wishartlab.com/queries/$queryId.json"
    logger.info(s"$id: Invoking url: $url")

    try {
      val result: ResponseEntity[QueryResult] = withRateLimit(id, "poll") {
        restOperations.getForEntity(url, classOf[QueryResult])
      }
      markUp()
      val resultBody: QueryResult = result.getBody

      if (resultBody.classification_status == "Done") {
        // The query has finished, so this is terminal either way: drop the query id and either store the
        // classification or give up. Not doing this would re-poll an invalid query forever
        if (resultBody.invalid_entities.isEmpty && resultBody.entities.nonEmpty) {
          logger.info(s"$id: ClassyFire query successful, fetching results")
          processClassification(compound, id, resultBody.entities.head)
        } else {
          logger.warn(s"$id: ClassyFire query finished without a usable classification, giving up")
          stat(_.incFailed())
          dropQueryId(compound)
        }
      } else {
        // Still in queue or in progress, keep the query id so the listener re-enqueues to poll again
        logger.info(s"$id: ClassyFire query not finished (status = ${resultBody.classification_status}), will poll again")
        compound
      }
    } catch {
      case x: HttpStatusCodeException =>
        logger.warn(s"$id: poll of query $queryId returned ${x.getStatusCode}, rescheduling")
        // The query expired or is unknown, drop the stale id and reschedule from structure
        dropQueryId(compound)
        scheduleClassification(compound, id)
      case x: ResourceAccessException =>
        markDown()
        logger.warn(s"$id: poll ClassyFire unreachable, will retry: ${x.getMessage}")
        markClassyfireUnavailable(compound, id)
      case x: RestClientException =>
        // The request succeeded but the body could not be read (e.g. an unexpected content type). Keep the
        // query id so the listener re-enqueues and polls again rather than silently dropping the spectrum
        logger.warn(s"$id: poll of query $queryId response could not be read, will poll again: ${x.getMessage}")
        compound
    }
  }

  /**
    * Remove the transient scheduled query id marker so the compound is no longer treated as pending
    *
    * @param compound
    * @return
    */
  private def dropQueryId(compound: Compound): Compound = {
    compound.setClassification(compound.getClassification.asScala.filterNot(_.getName == CommonMetaData.CLASSYFIRE_QUERY_ID).asJava)
    compound
  }

  /**
    * Look up an already classified compound by InChIKey. On a 404 the compound is novel, so schedule a query
    *
    * @param compound
    * @param id
    * @param inchiKey
    * @return
    */
  def lookupEntities(compound: Compound, id: String, inchiKey: String): Compound = {
    if (circuitOpen) {
      return markClassyfireUnavailable(compound, id)
    }

    val url = s"http://classyfire.wishartlab.com/entities/$inchiKey.json"
    logger.info(s"$id: Invoking url: $url")

    try {
      val result: ResponseEntity[ClassyfireResult] = withRateLimit(id, "entities") {
        restOperations.getForEntity(url, classOf[ClassyfireResult])
      }
      markUp()
      logger.info(s"$id: entities lookup successful")
      processClassification(compound, id, result.getBody)
    } catch {
      case x: HttpStatusCodeException if x.getStatusCode == HttpStatus.TOO_MANY_REQUESTS =>
        logger.warn(s"$id: entities lookup rate limited (429) after retries, leaving classification pending")
        stat(_.incRateLimited())
        markClassyfireUnavailable(compound, id)
      case x: HttpStatusCodeException =>
        logger.warn(s"$id: entities lookup returned ${x.getStatusCode} for $inchiKey, scheduling classification")
        scheduleClassification(compound, id)
      case x: ResourceAccessException =>
        markDown()
        logger.warn(s"$id: entities ClassyFire unreachable, will retry: ${x.getMessage}")
        markClassyfireUnavailable(compound, id)
      case x: RestClientException =>
        // The lookup succeeded but the body could not be read (e.g. an unexpected content type). Treat it like
        // a miss and schedule a fresh query rather than silently dropping the spectrum
        logger.warn(s"$id: entities lookup response could not be read for $inchiKey, scheduling classification: ${x.getMessage}")
        scheduleClassification(compound, id)
    }
  }

  /**
    * Turn a ClassyFire result into classification metadata, store it in the cache keyed by the result's
    * InChIKey skeleton, and set it on the compound (replacing any transient query id marker)
    *
    * @param compound
    * @param id
    * @param classification
    * @return
    */
  def processClassification(compound: Compound, id: String, classification: ClassyfireResult): Compound = {
    val buffer: ArrayBuffer[MetaData] = ArrayBuffer()

    if (classification.kingdom != null) {
      buffer += new MetaData(null, "kingdom", classification.kingdom.name, false, "classification", true, null)
    }

    if (classification.superclass != null) {
      buffer += new MetaData(null, "superclass", classification.superclass.name, false, "classification", true, null)
    }

    if (classification.`class` != null) {
      buffer += new MetaData(null, "class", classification.`class`.name, false, "classification", true, null)
    }

    if (classification.subclass != null) {
      buffer += new MetaData(null, "subclass", classification.subclass.name, false, "classification", true, null)
    }

    if (classification.intermediate_nodes != null) {
      var level = 1
      classification.intermediate_nodes.foreach { parent =>
        buffer += new MetaData(null, s"direct parent level $level", parent.name, false, "classification", true, null)
        level = level + 1
      }
    }

    if (classification.direct_parent != null && classification.direct_parent.name != null) {
      buffer += new MetaData(null, "direct parent", classification.direct_parent.name, false, "classification", true, null)
    }

    if (classification.alternative_parents != null) {
      classification.alternative_parents.foreach { parent =>
        buffer += new MetaData(null, "alternative parent", parent.name, false, "classification", true, null)
      }
    }

    if (buffer.nonEmpty) {
      stat(_.incNewlyClassified())

      // Cache by the classified structure's own skeleton so every spectrum sharing it reuses this result. The
      // ClassyFire response inchikey is prefixed (e.g. "InChIKey=AAAA..."), so strip it before validating and
      // keying, otherwise it fails the bare InChIKey pattern and would never match a lookup
      val responseKey: String = Option(classification.inchikey).map(_.stripPrefix("InChIKey=")).getOrElse("")
      if (isValidInchiKey(responseKey)) {
        storeClassificationCache(responseKey.take(14), buffer, id)
      }
    }

    compound.setClassification(buffer.asJava)
    compound
  }

  /**
    * Schedule classification of a novel compound from its structure and record the query id so the listener
    * re-enqueues the spectrum to poll for the result
    *
    * @param compound
    * @param id
    * @return
    */
  def scheduleClassification(compound: Compound, id: String): Compound = {
    if (circuitOpen) {
      return markClassyfireUnavailable(compound, id)
    }

    logger.info(s"$id: Scheduling compound classification from structure")

    val inchi: Buffer[MetaData] = compound.getMetaData.asScala.filter(x => x.getName == CommonMetaData.INCHI_CODE && x.getComputed)
    val smiles: mutable.Buffer[MetaData] = compound.getMetaData.asScala.filter(x => x.getName == CommonMetaData.SMILES && x.getComputed)

    val structure: String =
      if (inchi.nonEmpty) inchi.head.getValue.toString
      else if (smiles.nonEmpty) smiles.head.getValue.toString
      else ""

    if (structure.nonEmpty) {
      val url = s"http://classyfire.wishartlab.com/queries"
      logger.info(s"$id: Invoking url: $url")

      try {
        val result: ResponseEntity[QueryScheduleResult] = withRateLimit(id, "queries") {
          restOperations.postForEntity(url, QueryScheduleRequest("", structure, "STRUCTURE"), classOf[QueryScheduleResult])
        }
        markUp()
        logger.info(s"$id: Scheduled with query id ${result.getBody.id}")
        stat(_.incNewClassificationScheduled())
        compound.setClassification(ArrayBuffer[MetaData](new MetaData(null, CommonMetaData.CLASSYFIRE_QUERY_ID, result.getBody.id.toString, true, "none", false, null)).asJava)
        compound
      } catch {
        case x: HttpStatusCodeException =>
          logger.warn(s"$id: queries submission returned ${x.getStatusCode} for $structure, marking unavailable")
          stat(_.incFailed())
          markClassyfireUnavailable(compound, id)
        case x: ResourceAccessException =>
          markDown()
          logger.warn(s"$id: queries ClassyFire unreachable, will retry: ${x.getMessage}")
          markClassyfireUnavailable(compound, id)
        case x: RestClientException =>
          // The submission succeeded but the body could not be read (e.g. an unexpected content type), so the
          // query id is unknown. Mark it pending so it is retried rather than silently dropping the spectrum
          logger.warn(s"$id: queries submission response could not be read for $structure, marking unavailable: ${x.getMessage}")
          stat(_.incFailed())
          markClassyfireUnavailable(compound, id)
      }
    } else {
      logger.info(s"$id: No structure available to submit to ClassyFire")
      compound
    }
  }

  /**
    * Seed the skeleton cache from a compound that was already classified by an earlier run. Only writes on a
    * cache miss so re-curation does not repeatedly rewrite the same entry. Costs no ClassyFire call
    *
    * @param compound
    * @param id
    */
  def warmCacheFromExisting(compound: Compound, id: String): Unit = {
    val inchiKey: String = resolveInchiKey(compound)

    if (isValidInchiKey(inchiKey)) {
      val block: String = inchiKey.take(14)
      val existing: Buffer[MetaData] = compound.getClassification.asScala.filterNot(_.getName == CommonMetaData.CLASSYFIRE_QUERY_ID)

      if (existing.nonEmpty && classificationCacheLookup(block).isEmpty) {
        storeClassificationCache(block, existing, id)
      }
    }
  }

  /**
    * Look up a cached classification by skeleton block, swallowing any cache error
    *
    * @param block
    * @return
    */
  def classificationCacheLookup(block: String): Option[ClassificationCache] = {
    try {
      classificationCacheRestClient.findByBlock(block)
    } catch {
      case e: Throwable =>
        logger.warn(s"Unable to read classification cache for $block: ${e.getMessage}")
        None
    }
  }

  /**
    * Apply a cached classification to a compound, copying the metadata so the entries are inserted fresh
    *
    * @param compound
    * @param cache
    * @return
    */
  def applyCachedClassification(compound: Compound, cache: ClassificationCache): Compound = {
    val cached: Array[MetaData] = objectMapper.readValue(cache.getClassification, classOf[Array[MetaData]])
    compound.setClassification(cached.map(new MetaData(_)).toBuffer.asJava)
    compound
  }

  /**
    * Store a freshly computed classification in the cache, swallowing any cache error so it never blocks
    * the actual classification
    *
    * @param block
    * @param classification
    * @param id
    */
  def storeClassificationCache(block: String, classification: Buffer[MetaData], id: String): Unit = {
    try {
      val json: String = objectMapper.writeValueAsString(classification.asJava)
      classificationCacheRestClient.add(new ClassificationCache(block, json, new java.util.Date()))
      stat(_.incDbCacheWrite())
      logger.info(s"$id: Cached classification for skeleton $block")
    } catch {
      case e: Throwable =>
        logger.warn(s"$id: Unable to write classification cache for $block: ${e.getMessage}")
    }
  }

  /**
    * Run an outbound ClassyFire request, spacing it from the previous one and retrying on HTTP 429. The
    * spacing adapts: a 429 widens it (benefiting every endpoint since the timer is shared) and a run of
    * successes decays it back toward the floor. On a 429 the server's Retry-After header is honored when
    * present, otherwise an exponential backoff is used. Any non 429 error is rethrown for the caller to handle
    *
    * @param id       the spectrum id, for logging
    * @param endpoint which ClassyFire endpoint this call targets (entities, queries, poll), for logging
    * @param thunk    the request to run
    * @tparam T
    * @return
    */
  def withRateLimit[T](id: String, endpoint: String)(thunk: => T): T = {
    var attempt: Int = 0

    while (true) {
      pacingLock.synchronized {
        val spacing: Long = math.max(requestSpacingMs, currentSpacingMs)
        val wait: Long = lastRequestTime + spacing - System.currentTimeMillis()
        if (wait > 0) {
          Thread.sleep(wait)
        }
        lastRequestTime = System.currentTimeMillis()
      }

      try {
        val result: T = thunk
        recordSuccess()
        return result
      } catch {
        case e: HttpStatusCodeException if e.getStatusCode == HttpStatus.TOO_MANY_REQUESTS =>
          widenSpacing()
          attempt += 1
          if (attempt > maxRetries) {
            logger.warn(s"$id: $endpoint request still rate limited after $attempt attempts, giving up")
            throw e
          }
          val (backoff, source): (Long, String) = parseRetryAfter(e.getResponseHeaders) match {
            case Some(retryAfter) => (retryAfter, "server Retry-After")
            case None => (backoffBaseMs * (1L << (attempt - 1)), "computed backoff")
          }
          logger.warn(s"$id: $endpoint request rate limited (429), backing off ${backoff}ms ($source) and retrying ($attempt/$maxRetries)")
          Thread.sleep(backoff)
      }
    }

    // unreachable, the loop only exits via return or throw
    throw new IllegalStateException("rate limit loop exited unexpectedly")
  }

  /**
    * Widen the shared request spacing after a 429, up to the configured cap, and reset the success streak.
    * Multiplicative increase so the spacing climbs quickly toward ClassyFire's real limit
    */
  private[classyfire] def widenSpacing(): Unit = pacingLock.synchronized {
    val base: Long = math.max(requestSpacingMs, currentSpacingMs)
    currentSpacingMs = math.min(maxSpacingMs, math.round(base * spacingIncreaseFactor))
    consecutiveSuccesses = 0
  }

  /**
    * Record a successful request and, once enough have succeeded in a row, decay the spacing one step back
    * toward the floor. Additive decrease so the spacing eases down without oscillating
    */
  private[classyfire] def recordSuccess(): Unit = pacingLock.synchronized {
    consecutiveSuccesses += 1
    if (consecutiveSuccesses >= spacingRecoveryThreshold) {
      val base: Long = math.max(requestSpacingMs, currentSpacingMs)
      currentSpacingMs = math.max(requestSpacingMs, base - spacingDecayStepMs)
      consecutiveSuccesses = 0
    }
  }

  /**
    * The current effective spacing between requests, never below the configured floor
    */
  def currentSpacing: Long = pacingLock.synchronized(math.max(requestSpacingMs, currentSpacingMs))

  /**
    * Parse a Retry-After header into a millisecond delay. Supports both forms allowed by the spec, a number
    * of seconds or an HTTP date. Returns None when the header is absent or unparseable so the caller falls
    * back to its own backoff. A delay in the past is clamped to zero
    *
    * @param headers the response headers from the 429, may be null
    * @return the delay in milliseconds, if a usable Retry-After was present
    */
  private[classyfire] def parseRetryAfter(headers: HttpHeaders): Option[Long] = {
    if (headers == null) {
      None
    } else {
      Option(headers.getFirst(HttpHeaders.RETRY_AFTER)).map(_.trim).filter(_.nonEmpty).flatMap { value =>
        Try(value.toLong).toOption.map(_ * 1000L).orElse {
          Try(ZonedDateTime.parse(value, DateTimeFormatter.RFC_1123_DATE_TIME))
            .toOption.map(_.toInstant.toEpochMilli - System.currentTimeMillis())
        }
      }.map(math.max(0L, _))
    }
  }

  /**
    * Whether ClassyFire is currently believed reachable. The listener re-enqueues a spectrum processed while
    * the service was down so it is retried once the service recovers, rather than silently left unclassified
    *
    * @return
    */
  def serviceAvailable: Boolean = serviceUp.get

  private def circuitOpen: Boolean = !serviceUp.get && (System.currentTimeMillis() - downSince < downCooldownMs)

  private def markDown(): Unit = {
    serviceUp.set(false)
    downSince = System.currentTimeMillis()
  }

  private def markUp(): Unit = serviceUp.set(true)
}
