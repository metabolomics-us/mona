package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.spectrum

import java.util.concurrent.Future
import java.util.{Date, UUID}
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.HelperTypes.{LoginInfo, WrappedString}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.service.SpectrumPersistenceService
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.data.domain.{Page, PageRequest, Pageable, Sort}
import org.springframework.http.{HttpHeaders, HttpStatus, ResponseEntity}
import org.springframework.scheduling.annotation.{Async, AsyncResult}
import org.springframework.web.bind.annotation._
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{DeletionJob, Spectrum, SpectrumDeletionRequest, SpectrumSubmitter, Submitter}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.util.DynamicIterable
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
    * Executes a search against the repository and can cause out of memory errors.  It is recommended to utilize this
    * method with pagination
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
    def sendQuery(query: String, page: Integer, size: Integer): Iterable[Spectrum] = {

      if (size != null) {
        if (page != null) {
          val test = spectrumPersistenceService.findAll(query, PageRequest.of(page, size)).getContent.asScala
          test
        } else {
          val test = spectrumPersistenceService.findAll(query, PageRequest.of(0, size)).getContent.asScala
          test
        }
      } else {
        val test = spectrumPersistenceService.findAll(query).asScala
        logger.info(s"Controller return size: ${test.size}")
        test
      }
    }

    if (query != null) {
      val queryString = if (query != null) query.string else ""

      new AsyncResult[ResponseEntity[Iterable[Spectrum]]](
        new ResponseEntity(sendQuery(queryString, page, size), HttpStatus.OK)
      )
    } else {
      new AsyncResult[ResponseEntity[Iterable[Spectrum]]](new ResponseEntity(HttpStatus.BAD_REQUEST))
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
   * Returns all the specified data in the system.  Should be utilized with pagination to avoid
   * out of memory issues
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
    val data: Iterable[Spectrum] = {
      if (size != null) {
        if (page != null) {
          spectrumPersistenceService.findAll(PageRequest.of(page, size, Sort.Direction.ASC, "id")).getContent.asScala
        } else {
          spectrumPersistenceService.findAll(PageRequest.of(0, size, Sort.Direction.ASC, "id")).getContent.asScala
        }
      } else {
        new DynamicIterable[Spectrum, String]("", 50) {
          // loads more data from the server for the given query
          override def fetchMoreData(query: String, pageable: Pageable): Page[Spectrum] = spectrumPersistenceService.findAll(pageable)
        }.asScala
      }
    }
    val headers = new HttpHeaders()
    // headers.add("Content-Type", servletRequest.getContentType)

    new AsyncResult[ResponseEntity[Iterable[Spectrum]]](
      new ResponseEntity(data, headers, HttpStatus.OK)
    )
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
