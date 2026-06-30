package edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound.classyfire

import org.springframework.stereotype.Component

import java.util.concurrent.atomic.AtomicLong

/**
  * Running counters for a ClassyFire classification batch, shared by the processor and the queue listener.
  * They feed a grouped summary logged when the dedicated queue drains, then are reset for the next batch.
  *
  * The counters are in three different units, which is why the summary groups them:
  *   - spectra: counted per queue message
  *   - compound outcomes: counted per compound (a spectrum has one or more compounds), each compound lands
  *     in exactly one outcome
  *   - cache writes: a side action, not mutually exclusive with the outcomes (a newly classified compound and
  *     an already classified compound seeding the cache both write)
  */
@Component
class ClassyfireStats {

  // Per spectrum (queue messages)
  val spectraReceived = new AtomicLong(0)     // spectra pulled from the classyfire queue
  val pending = new AtomicLong(0)             // spectra re-enqueued to poll a scheduled query or retry an outage

  // Per compound outcome. Every processed compound lands in exactly one of these, so for a single compound
  // per spectrum dataset their sum reconciles with spectraReceived (a re-enqueued spectrum is received and
  // counted again, and each such receive also lands in exactly one outcome)
  val newlyClassified = new AtomicLong(0)              // freshly classified by a scheduled query we submitted
  val entitiesHits = new AtomicLong(0)                 // classification fetched from ClassyFire's entities endpoint
  val dbCacheHits = new AtomicLong(0)                  // classification reused from the skeleton cache
  val alreadyClassified = new AtomicLong(0)            // skipped, the compound already had classification
  val missingInchiKey = new AtomicLong(0)              // skipped, no valid InChIKey
  val newClassificationsScheduled = new AtomicLong(0)  // novel structures submitted for async classification
  val awaitingPoll = new AtomicLong(0)                 // scheduled query polled but not finished, re-enqueued
  val noStructure = new AtomicLong(0)                  // valid InChIKey but no structure to submit, left unclassified
  val serviceUnavailable = new AtomicLong(0)           // ClassyFire down or unreachable, re-enqueued to retry
  val rateLimited = new AtomicLong(0)                  // abandoned after repeated HTTP 429s
  val failed = new AtomicLong(0)                       // failed (invalid query result or schedule error)

  // Cache writes (side action, overlaps the outcomes above)
  val dbCacheWrites = new AtomicLong(0)       // classifications written to the skeleton cache

  // Live gauge of spectra currently outstanding in the classyfire queue (published but not yet terminal).
  // Unlike the batch counters above it is never reset, so it can be polled at any time, e.g. to guard the
  // re-curation button against being pressed again while classification is still draining
  private val queueDepth = new AtomicLong(0)

  def incQueued(): Long = queueDepth.incrementAndGet()

  // Decrement but never report below zero, so a missed decrement on an unexpected error cannot make it negative
  def decQueued(): Long = {
    val value = queueDepth.decrementAndGet()
    if (value < 0) {
      queueDepth.set(0)
      0
    } else {
      value
    }
  }

  def currentQueueDepth: Long = queueDepth.get()

  def incSpectraReceived(): Unit = spectraReceived.incrementAndGet()
  def incPending(): Unit = pending.incrementAndGet()
  def incNewlyClassified(): Unit = newlyClassified.incrementAndGet()
  def incEntitiesHit(): Unit = entitiesHits.incrementAndGet()
  def incDbCacheHit(): Unit = dbCacheHits.incrementAndGet()
  def incAlreadyClassified(): Unit = alreadyClassified.incrementAndGet()
  def incMissingInchiKey(): Unit = missingInchiKey.incrementAndGet()
  def incNewClassificationScheduled(): Unit = newClassificationsScheduled.incrementAndGet()
  def incAwaitingPoll(): Unit = awaitingPoll.incrementAndGet()
  def incNoStructure(): Unit = noStructure.incrementAndGet()
  def incServiceUnavailable(): Unit = serviceUnavailable.incrementAndGet()
  def incRateLimited(): Unit = rateLimited.incrementAndGet()
  def incFailed(): Unit = failed.incrementAndGet()
  def incDbCacheWrite(): Unit = dbCacheWrites.incrementAndGet()

  /**
    * True if anything has been processed since the last reset, used to avoid logging an empty summary
    */
  def hasActivity: Boolean = spectraReceived.get() > 0

  def summary: String =
    "ClassyFire batch summary:\n" +
      s"  spectra:           spectraReceived=${spectraReceived.get()}, pendingReEnqueued=${pending.get()}\n" +
      s"  compound outcomes: newlyClassified=${newlyClassified.get()}, entitiesHits=${entitiesHits.get()}, " +
      s"dbCacheHits=${dbCacheHits.get()}, alreadyClassified=${alreadyClassified.get()}, " +
      s"missingInchiKey=${missingInchiKey.get()}, newClassificationsScheduled=${newClassificationsScheduled.get()}, " +
      s"awaitingPoll=${awaitingPoll.get()}, noStructure=${noStructure.get()}, " +
      s"serviceUnavailable=${serviceUnavailable.get()}, rateLimited=${rateLimited.get()}, " +
      s"failed=${failed.get()}\n" +
      s"  cache writes:      dbCacheWrites=${dbCacheWrites.get()}"

  def reset(): Unit = {
    spectraReceived.set(0)
    pending.set(0)
    newlyClassified.set(0)
    entitiesHits.set(0)
    dbCacheHits.set(0)
    alreadyClassified.set(0)
    missingInchiKey.set(0)
    newClassificationsScheduled.set(0)
    awaitingPoll.set(0)
    noStructure.set(0)
    serviceUnavailable.set(0)
    rateLimited.set(0)
    failed.set(0)
    dbCacheWrites.set(0)
  }
}
