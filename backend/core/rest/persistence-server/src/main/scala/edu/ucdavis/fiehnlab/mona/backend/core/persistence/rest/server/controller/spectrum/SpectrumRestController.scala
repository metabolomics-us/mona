package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.spectrum

import java.util.concurrent.Future
import java.util.{Date, UUID}
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.HelperTypes.{LoginInfo, WrappedString}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.service.SpectrumPersistenceService
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.data.domain.{PageRequest, Sort}
import org.springframework.http.{HttpHeaders, HttpStatus, ResponseEntity}
import org.springframework.scheduling.annotation.{Async, AsyncResult}
import org.springframework.web.bind.annotation._
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{DeletionJob, Spectrum, SpectrumDeletionRequest, SpectrumSubmitter, Submitter}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.{DeletionJobRepository, SubmitterRepository}
import org.springframework.amqp.core.MessageDeliveryMode
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.context.annotation.Profile

import javax.servlet.{ServletRequest, ServletResponse}
import javax.validation.Valid
import scala.jdk.CollectionConverters._

@CrossOrigin
@RestController
@RequestMapping(Array("/rest/spectra"))
@Profile(Array("mona.persistence"))
class SpectrumRestController extends LazyLogging {

  @Autowired
  val httpServletRequest: HttpServletRequest = null

  @Autowired
  val spectrumPersistenceService: SpectrumPersistenceService = null

  @Autowired
  val submitterRepository: SubmitterRepository = null

  @Autowired
  val loginService: LoginService = null

  @Autowired
  val deletionJobRepository: DeletionJobRepository = null

  @Autowired
  val rabbitTemplate: RabbitTemplate = null

  @Autowired
  @Qualifier("spectra-deletion-queue")
  val deletionQueueName: String = null

  // Default page size applied when a list/search request omits size, and the hard maximum for
  // explicit sizes
  val maxPageSize = 50000

  /**
    * Builds the response headers for a request whose size was defaulted to maxPageSize. X-Page-Size
    * announces the applied default. When the page came back full, more results may exist, so a
    * RFC 8288 Link rel="next" header pointing at the following page is added as well, built from
    * the current request with any page/size parameters rewritten
    */
  private def defaultedPageHeaders(page: Int, resultCount: Int): HttpHeaders = {
    val headers = new HttpHeaders()
    headers.add("X-Page-Size", maxPageSize.toString)

    if (resultCount == maxPageSize) {
      val retainedParams = Option(httpServletRequest.getQueryString).getOrElse("")
        .split("&").filter(_.nonEmpty)
        .filterNot(param => param.startsWith("page=") || param.startsWith("size="))
      val nextParams = (retainedParams :+ s"page=${page + 1}" :+ s"size=$maxPageSize").mkString("&")
      headers.add(HttpHeaders.LINK, s"<${httpServletRequest.getRequestURL}?$nextParams>; rel=\"next\"")
    }

    headers
  }

  /**
    * rejects an explicit size above maxPageSize loudly instead of silently truncating
    */
  private def oversizeError[T]: ResponseEntity[T] = {
    val body: java.util.Map[String, String] = java.util.Map.of(
      "error", s"requested size exceeds the maximum of $maxPageSize, use page/size pagination or the downloads API for bulk exports"
    )
    new ResponseEntity(body, HttpStatus.BAD_REQUEST).asInstanceOf[ResponseEntity[T]]
  }

  /**
    * resolves the email of the caller from the Authorization header so deletion jobs can record
    * who triggered them. Returns null when no usable token is present
    */
  private def callerEmail(): String = {
    val header = httpServletRequest.getHeader("Authorization")

    if (header != null && header.split(" ").length > 1) {
      try {
        loginService.info(header.split(" ").last).emailAddress
      } catch {
        case e: Exception =>
          logger.debug(s"could not resolve caller email: ${e.getMessage}")
          null
      }
    } else {
      null
    }
  }

  /**
    * enqueues a deletion job on the durable queue with persistent delivery so it survives a
    * broker restart, then returns the freshly persisted tracking row
    */
  private def enqueueDeletionJob(job: DeletionJob): Unit = {
    deletionJobRepository.save(job)
    rabbitTemplate.convertAndSend(deletionQueueName, new SpectrumDeletionRequest(job.getId), (message: org.springframework.amqp.core.Message) => {
      message.getMessageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT)
      message
    })
  }

  /**
    * Executes a search against the repository. Requests without a size get a default page of
    * maxPageSize. Sizes above maxPageSize are rejected with a 400
    *
    * @param query
    * @return
    */
  @RequestMapping(path = Array("/search"), method = Array(RequestMethod.GET), produces = Array("application/json", "text/msp"))
  @Async
  @ResponseBody
  def searchRSQL(@RequestParam(value = "page", required = false) page: Integer,
                 @RequestParam(value = "size", required = false) size: Integer,
                 @RequestParam(value = "query", required = false) query: WrappedString,
                 request: HttpServletRequest, response: HttpServletResponse): Future[ResponseEntity[Iterable[Spectrum]]] = {
    if (query == null) {
      new AsyncResult[ResponseEntity[Iterable[Spectrum]]](new ResponseEntity(HttpStatus.BAD_REQUEST))
    } else if (size != null && size > maxPageSize) {
      new AsyncResult[ResponseEntity[Iterable[Spectrum]]](oversizeError)
    } else {
      val effectivePage: Int = if (page != null) page.toInt else 0

      if (size != null) {
        val content = spectrumPersistenceService.findAll(query.string, PageRequest.of(effectivePage, size.toInt)).getContent.asScala
        new AsyncResult[ResponseEntity[Iterable[Spectrum]]](new ResponseEntity(content, HttpStatus.OK))
      } else {
        val content = spectrumPersistenceService.findAll(query.string, PageRequest.of(effectivePage, maxPageSize)).getContent.asScala
        new AsyncResult[ResponseEntity[Iterable[Spectrum]]](
          new ResponseEntity(content, defaultedPageHeaders(effectivePage, content.size), HttpStatus.OK)
        )
      }
    }
  }

  // Keyword terms below this length cannot use the trigram indexes and would degrade to full
  // scans, so they are rejected loudly
  val minKeywordLength = 3

  private def keywordError[T]: ResponseEntity[T] = {
    val body: java.util.Map[String, String] = java.util.Map.of(
      "error", s"keyword searches require a query of at least $minKeywordLength characters"
    )
    new ResponseEntity(body, HttpStatus.BAD_REQUEST).asInstanceOf[ResponseEntity[T]]
  }

  /**
    * Case insensitive contains search over metadata values and compound names, used by the search
    * box. Runs as a per branch trigram indexed lookup instead of the generic filter path, whose OR
    * across joined tables cannot use any index. Same pagination contract as /search
    *
    * @param query the raw search term, matched as a substring
    * @return
    */
  @RequestMapping(path = Array("/keyword"), method = Array(RequestMethod.GET), produces = Array("application/json"))
  @Async
  @ResponseBody
  def keywordSearch(@RequestParam(value = "page", required = false) page: Integer,
                    @RequestParam(value = "size", required = false) size: Integer,
                    @RequestParam(value = "query", required = false) query: WrappedString): Future[ResponseEntity[Iterable[Spectrum]]] = {
    if (query == null || query.string.trim.length < minKeywordLength) {
      new AsyncResult[ResponseEntity[Iterable[Spectrum]]](keywordError)
    } else if (size != null && size > maxPageSize) {
      new AsyncResult[ResponseEntity[Iterable[Spectrum]]](oversizeError)
    } else {
      val effectivePage: Int = if (page != null) page.toInt else 0

      if (size != null) {
        val content = spectrumPersistenceService.findByKeyword(query.string.trim, effectivePage, size.toInt).asScala
        new AsyncResult[ResponseEntity[Iterable[Spectrum]]](new ResponseEntity(content, HttpStatus.OK))
      } else {
        val content = spectrumPersistenceService.findByKeyword(query.string.trim, effectivePage, maxPageSize).asScala
        new AsyncResult[ResponseEntity[Iterable[Spectrum]]](
          new ResponseEntity(content, defaultedPageHeaders(effectivePage, content.size), HttpStatus.OK)
        )
      }
    }
  }

  /**
    * Returns the count of spectra the keyword search would match
    *
    * @param query the raw search term, matched as a substring
    * @return
    */
  @RequestMapping(path = Array("/keyword/count"), method = Array(RequestMethod.GET))
  @Async
  @ResponseBody
  def keywordCount(@RequestParam(value = "query", required = false) query: WrappedString): Future[ResponseEntity[Long]] = {
    if (query == null || query.string.trim.length < minKeywordLength) {
      new AsyncResult[ResponseEntity[Long]](keywordError)
    } else {
      new AsyncResult[ResponseEntity[Long]](new ResponseEntity(spectrumPersistenceService.countByKeyword(query.string.trim), HttpStatus.OK))
    }
  }

  /**
    * Returns the counts of objects, which would be received by the given query
    *
    * @return
    */
  @RequestMapping(path = Array("/search/count"), method = Array(RequestMethod.GET))
  @Async
  @ResponseBody
  def searchCount(@RequestParam(value = "query", required = false) query: WrappedString): Future[Long] = {
    if ((query != null && query.string.nonEmpty)) {
      val queryString = if (query != null) query.string else ""

      new AsyncResult[Long](spectrumPersistenceService.count(queryString))
    } else {
      new AsyncResult[Long](spectrumPersistenceService.count())
    }
  }

  /**
    * enqueues a deletion of the given spectra ids as a tracked background job and returns
    * immediately with the job (status SCHEDULED). Poll /rest/spectra/delete/status/{id} for progress
    */
  @RequestMapping(path = Array(""), method = Array(RequestMethod.DELETE))
  @ResponseBody
  def massDeleteByID(@RequestBody ids: java.util.List[String]): ResponseEntity[DeletionJob] = {
    if (ids == null || ids.isEmpty) {
      logger.warn("refusing id based deletion with an empty id list")
      new ResponseEntity[DeletionJob](HttpStatus.BAD_REQUEST)
    } else {
      val job = new DeletionJob(UUID.randomUUID.toString, null, ids.asScala.mkString(","), callerEmail(), new Date, DeletionJob.STATUS_SCHEDULED, ids.size.toLong)
      enqueueDeletionJob(job)
      new ResponseEntity[DeletionJob](job, HttpStatus.ACCEPTED)
    }
  }

  /**
    * enqueues a deletion of all spectra matching the query as a tracked background job and returns
    * immediately with the job (status SCHEDULED). An empty query is rejected so a blank request can
    * never match (and delete) everything. Poll /rest/spectra/delete/status/{id} for progress
    */
  @RequestMapping(path = Array("/search"), method = Array(RequestMethod.DELETE))
  @ResponseBody
  def massDeleteBySearch(@RequestParam(value = "query", required = false) query: WrappedString): ResponseEntity[DeletionJob] = {
    val queryString = if (query != null) query.string else ""

    if (queryString.trim.isEmpty) {
      logger.warn("refusing query based deletion with an empty query (would match everything)")
      new ResponseEntity[DeletionJob](HttpStatus.BAD_REQUEST)
    } else {
      val total = spectrumPersistenceService.count(queryString)
      val job = new DeletionJob(UUID.randomUUID.toString, queryString, null, callerEmail(), new Date, DeletionJob.STATUS_SCHEDULED, total)
      enqueueDeletionJob(job)
      new ResponseEntity[DeletionJob](job, HttpStatus.ACCEPTED)
    }
  }

  /**
    * returns the current state of a deletion job (status, total, deleted, skipped) for progress polling
    */
  @RequestMapping(path = Array("/delete/status/{jobId}"), method = Array(RequestMethod.GET))
  @ResponseBody
  def deletionStatus(@PathVariable("jobId") jobId: String): ResponseEntity[DeletionJob] = {
    val job = deletionJobRepository.findById(jobId).orElse(null)

    if (job == null) {
      new ResponseEntity[DeletionJob](HttpStatus.NOT_FOUND)
    } else {
      new ResponseEntity[DeletionJob](job, HttpStatus.OK)
    }
  }

  /**
   * Returns all the specified data in the system. Requests without a size get a default page of
   * maxPageSize (announced via the X-Page-Size header, with a Link rel="next" header when the
   * page came back full), sizes above maxPageSize are rejected with a 400
   *
   * @return
   */
  @RequestMapping(path = Array(""), method = Array(RequestMethod.GET), produces = Array("application/json", "text/msp", "text/sdf", "image/png"))
  @Async
  @ResponseBody
  final def list(@RequestParam(value = "page", required = false) page: Integer, @RequestParam(value = "size", required = false) size: Integer): Future[ResponseEntity[Iterable[Spectrum]]] = {
    doList(page, size)
  }

  def doList(page: Integer, size: Integer): Future[ResponseEntity[Iterable[Spectrum]]] = {
    if (size != null && size > maxPageSize) {
      new AsyncResult[ResponseEntity[Iterable[Spectrum]]](oversizeError)
    } else {
      val effectivePage: Int = if (page != null) page.toInt else 0

      if (size != null) {
        val data = spectrumPersistenceService.findAll(PageRequest.of(effectivePage, size.toInt, Sort.Direction.ASC, "id")).getContent.asScala
        new AsyncResult[ResponseEntity[Iterable[Spectrum]]](new ResponseEntity(data, HttpStatus.OK))
      } else {
        val data = spectrumPersistenceService.findAll(PageRequest.of(effectivePage, maxPageSize, Sort.Direction.ASC, "id")).getContent.asScala
        new AsyncResult[ResponseEntity[Iterable[Spectrum]]](
          new ResponseEntity(data, defaultedPageHeaders(effectivePage, data.size), HttpStatus.OK)
        )
      }
    }
  }

  /**
   * Returns the complete count of resources in the system
   *
   * @return
   */
  @RequestMapping(path = Array("/count"), method = Array(RequestMethod.GET))
  @Async
  @ResponseBody
  final def searchCount: Future[Long] = {
    new AsyncResult[Long](spectrumPersistenceService.count())
  }

  /**
   * Saves a resource or updates it
   *
   * @param resource
   * @return
   */
  @Async
  @RequestMapping(path = Array(""), method = Array(RequestMethod.POST))
  @ResponseBody
  final def save(@Valid @RequestBody resource: Spectrum): Future[ResponseEntity[Spectrum]] = {
    doSave(resource)
  }

  def finalSave(resource: Spectrum): Future[ResponseEntity[Spectrum]] = {
    new AsyncResult[ResponseEntity[Spectrum]](
      new ResponseEntity[Spectrum](spectrumPersistenceService.save(resource), HttpStatus.OK)
    )
  }

  /**
    * Saves a spectrum
    *
    * @param spectrum
    * @return
    */
  def doSave(spectrum: Spectrum): Future[ResponseEntity[Spectrum]] = {
    val token: String = httpServletRequest.getHeader("Authorization").split(" ").last
    val loginInfo: LoginInfo = loginService.info(token)

    val existingSubmitter: Submitter = submitterRepository.findTopByEmailAddress(loginInfo.emailAddress)

    // Admins can save anything
    if (loginInfo.roles.contains("ADMIN")) {
      if (spectrum.getId == null || !spectrumPersistenceService.existsById(spectrum.getId)) {
        finalSave(spectrum)
      } else {
        val existingSpectrum: Spectrum = spectrumPersistenceService.findByMonaId(spectrum.getId)
        spectrum.setDateCreated(existingSpectrum.getDateCreated)
        finalSave(spectrum)
      }
    }

    // If a user has no submitter information, we cannot accept the spectrum
    else if (existingSubmitter == null) {
      new AsyncResult[ResponseEntity[Spectrum]](new ResponseEntity[Spectrum](HttpStatus.FORBIDDEN))
    }

    // If no id is provided, a new record can be added with no issues
    else if (spectrum.getId == null || spectrum.getId.isEmpty) {
      val spectrumSubmitter = new SpectrumSubmitter(existingSubmitter.getEmailAddress, existingSubmitter.getFirstName, existingSubmitter.getLastName, existingSubmitter.getInstitution)
      spectrum.setId(null)
      spectrum.setSubmitter(spectrumSubmitter)
      finalSave(spectrum)
    }

    // Check whether a spectrum with the given id exists.  If it does, the submitter
    // must own it to update it.  Otherwise, the request is not allowed
    else {
      val existingSpectrum: Spectrum = spectrumPersistenceService.findByMonaId(spectrum.getId)

      if (existingSpectrum == null) {
        finalSave(spectrum)
      } else if (existingSpectrum.getSubmitter.getEmailAddress == loginInfo.emailAddress) {
        val spectrumSubmitter = new SpectrumSubmitter(existingSubmitter.getEmailAddress, existingSubmitter.getFirstName, existingSubmitter.getLastName, existingSubmitter.getInstitution)
        spectrum.setDateCreated(existingSpectrum.getDateCreated)
        spectrum.setSubmitter(spectrumSubmitter)
        spectrum.setDateCreated(existingSpectrum.getDateCreated)
        finalSave(spectrum)
      } else {
        new AsyncResult[ResponseEntity[Spectrum]](new ResponseEntity[Spectrum](HttpStatus.CONFLICT))
      }
    }
  }

  /**
   * Returns the specified resource
   *
   * @param id
   * @return
   */
  @Async
  @RequestMapping(path = Array("/{id}"), method = Array(RequestMethod.GET), produces = Array("application/json", "text/msp", "text/sdf", "image/png"))
  @ResponseBody
  final def get(@PathVariable("id") id: String, servletRequest: ServletRequest, servletResponse: ServletResponse): Future[ResponseEntity[Spectrum]] = {
    doGet(id, servletRequest, servletResponse)
  }

  def doGet(id: String, servletRequest: ServletRequest, servletResponse: ServletResponse): Future[ResponseEntity[Spectrum]] = {
    val headers = new HttpHeaders()
    // headers.add("Content-Type", servletRequest.getContentType)

    if (spectrumPersistenceService.existsById(id)) {
      new AsyncResult[ResponseEntity[Spectrum]](new ResponseEntity[Spectrum](spectrumPersistenceService.findByMonaId(id), headers, HttpStatus.OK))
    } else {
      new AsyncResult[ResponseEntity[Spectrum]](new ResponseEntity[Spectrum](HttpStatus.NOT_FOUND))
    }
  }


  /**
   * Removes the specified resource from the system
   *
   * @param id
   * @return
   */
  @RequestMapping(path = Array("/{id}"), method = Array(RequestMethod.DELETE))
  @ResponseBody
  final def delete(@PathVariable("id") id: String): ResponseEntity[String] = {
    doDelete(id)
  }

  /**
    * deletes a single spectrum. Allowed for an admin or the spectrum's owner only. Routes through
    * the event firing delete so listeners (e.g. webhooks) are notified
    */
  def doDelete(id: String): ResponseEntity[String] = {
    val spectrum: Spectrum = spectrumPersistenceService.findByMonaId(id)

    if (spectrum == null) {
      new ResponseEntity[String](HttpStatus.NOT_FOUND)
    } else {
      val token: String = httpServletRequest.getHeader("Authorization").split(" ").last
      val loginInfo: LoginInfo = loginService.info(token)

      val isAdmin: Boolean = loginInfo.roles.contains("ADMIN")
      val isOwner: Boolean = spectrum.getSubmitter != null && spectrum.getSubmitter.getEmailAddress == loginInfo.emailAddress

      if (isAdmin || isOwner) {
        spectrumPersistenceService.delete(spectrum)
        // empty body on success so the generic rest client can deserialize the response without
        // tripping over a non json string. The 200 status communicates the delete succeeded
        new ResponseEntity[String](HttpStatus.OK)
      } else {
        logger.warn(s"user ${loginInfo.emailAddress} attempted to delete spectrum $id they do not own")
        new ResponseEntity[String](HttpStatus.FORBIDDEN)
      }
    }
  }


  /**
   * Saves the provided resource at the given path
   *
   * @param id
   * @param resource
   * @return
   */
  @Async
  @RequestMapping(path = Array("/{id}"), method = Array(RequestMethod.PUT))
  @ResponseBody
  final def put(@PathVariable("id") id: String, @Valid @RequestBody resource: Spectrum): Future[ResponseEntity[Spectrum]] = {
    doPut(id, resource)
  }

  /**
    * Saves the provided spectrum at the given path
    *
    * @param id
    * @param spectrum
    * @return
    */
  def doPut(id: String, spectrum: Spectrum): Future[ResponseEntity[Spectrum]] = {
    val token: String = httpServletRequest.getHeader("Authorization").split(" ").last
    val loginInfo: LoginInfo = loginService.info(token)
    val existingSubmitter: Submitter = submitterRepository.findTopByEmailAddress(loginInfo.emailAddress)

    // Admins can save anything
    if (loginInfo.roles.contains("ADMIN")) {
      if (spectrum.getId == null || spectrum.getId == "" || spectrum.getId == id) {
        if (spectrum.getId != null && !spectrumPersistenceService.existsById(spectrum.getId)) {
          spectrum.setId(id)
          finalSave(spectrum)
        } else {
          val existingOldSpectrum: Spectrum = spectrumPersistenceService.findByMonaId(spectrum.getId)
          spectrum.setId(id)
          spectrum.setDateCreated(existingOldSpectrum.getDateCreated)
          finalSave(spectrum)
        }
      } else {
        val existingOldSpectrum: Spectrum = spectrumPersistenceService.findByMonaId(spectrum.getId)
        val existingNewSpectrum: Spectrum = spectrumPersistenceService.findByMonaId(id)

        spectrumPersistenceService.deleteById(spectrum.getId)

        if (existingOldSpectrum != null) {
          spectrum.setId(id)
          spectrum.setDateCreated(existingOldSpectrum.getDateCreated)
          finalSave(spectrum)
        } else if (existingNewSpectrum != null) {
          spectrum.setId(id)
          spectrum.setDateCreated(existingNewSpectrum.getDateCreated)
          finalSave(spectrum)
        } else {
          spectrum.setId(id)
          finalSave(spectrum)
        }
      }
    }

    // If a user has no submitter information, we cannot accept the spectrum
    else if (existingSubmitter == null) {
      new AsyncResult[ResponseEntity[Spectrum]](new ResponseEntity[Spectrum](HttpStatus.FORBIDDEN))
    }

    // User should be able to update or change the id their own spectra
    else {
      // Handle the case of saving a new spectrum/updating record $id
      if (spectrum.getId == null || spectrum.getId.isEmpty || spectrum.getId == id) {
        if (spectrum.getId != null && !spectrumPersistenceService.existsById(spectrum.getId)) {
          val spectrumSubmitter = new SpectrumSubmitter(existingSubmitter.getEmailAddress, existingSubmitter.getFirstName, existingSubmitter.getLastName, existingSubmitter.getInstitution)
          spectrum.setId(id)
          spectrum.setSubmitter(spectrumSubmitter)
          finalSave(spectrum)
        } else {
          val existingOldSpectrum: Spectrum = spectrumPersistenceService.findByMonaId(spectrum.getId)

          if (existingOldSpectrum.getSubmitter == null || existingOldSpectrum.getSubmitter.getEmailAddress == loginInfo.emailAddress) {
            val spectrumSubmitter = new SpectrumSubmitter(existingSubmitter.getEmailAddress, existingSubmitter.getFirstName, existingSubmitter.getLastName, existingSubmitter.getInstitution)

            spectrum.setId(id)
            spectrum.setDateCreated(existingOldSpectrum.getDateCreated)
            spectrum.setSubmitter(spectrumSubmitter)
            finalSave(spectrum)
          } else {
            new AsyncResult[ResponseEntity[Spectrum]](new ResponseEntity[Spectrum](HttpStatus.FORBIDDEN))
          }
        }
      }

      // Handle the case of differing ids
      else {
        val existingOldSpectrum: Spectrum = spectrumPersistenceService.findByMonaId(spectrum.getId)
        val existingNewSpectrum: Spectrum = spectrumPersistenceService.findByMonaId(id)

        if (existingOldSpectrum != null && existingOldSpectrum.getSubmitter.getEmailAddress != loginInfo.emailAddress) {
          // Not allowed to delete old spectrum if it belongs to someone else
          new AsyncResult[ResponseEntity[Spectrum]](new ResponseEntity[Spectrum](HttpStatus.CONFLICT))
        } else if (existingNewSpectrum != null && existingNewSpectrum.getSubmitter.getEmailAddress != loginInfo.emailAddress) {
          // Not allowed to update the new spectrum if it belongs to someone else
          new AsyncResult[ResponseEntity[Spectrum]](new ResponseEntity[Spectrum](HttpStatus.CONFLICT))
        } else {
          spectrumPersistenceService.deleteById(spectrum.getId)

          // Use the old dateCreated field
          if (existingOldSpectrum != null) {
            val spectrumSubmitter = new SpectrumSubmitter(existingSubmitter.getEmailAddress, existingSubmitter.getFirstName, existingSubmitter.getLastName, existingSubmitter.getInstitution)

            spectrum.setId(id)
            spectrum.setDateCreated(existingOldSpectrum.getDateCreated)
            spectrum.setSubmitter(spectrumSubmitter)
            finalSave(spectrum)
          } else if (existingNewSpectrum != null) {
            val spectrumSubmitter = new SpectrumSubmitter(existingSubmitter.getEmailAddress, existingSubmitter.getFirstName, existingSubmitter.getLastName, existingSubmitter.getInstitution)

            spectrum.setId(id)
            spectrum.setDateCreated(existingNewSpectrum.getDateCreated)
            spectrum.setSubmitter(spectrumSubmitter)
            finalSave(spectrum)
          } else {
            val spectrumSubmitter = new SpectrumSubmitter(existingSubmitter.getEmailAddress, existingSubmitter.getFirstName, existingSubmitter.getLastName, existingSubmitter.getInstitution)

            spectrum.setId(id)
            spectrum.setSubmitter(spectrumSubmitter)
            finalSave(spectrum)
          }
        }
      }
    }
  }
}
