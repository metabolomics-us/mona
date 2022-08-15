package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.spectrum

import java.util.concurrent.Future
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.HelperTypes.{LoginInfo, WrappedString}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.service.SpectrumPersistenceService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.{Page, PageRequest, Pageable, Sort}
import org.springframework.http.{HttpHeaders, HttpStatus, ResponseEntity}
import org.springframework.scheduling.annotation.{Async, AsyncResult}
import org.springframework.web.bind.annotation.{RequestMapping, _}
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.util.DynamicIterable
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.dao.SubmitterDAO
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.{SpectrumResult, Submitter}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.SubmitterRepository

import javax.servlet.{ServletRequest, ServletResponse}
import javax.validation.Valid
import scala.jdk.CollectionConverters._

@CrossOrigin
@RestController
@RequestMapping(Array("/rest/spectra"))
class SpectrumRestController extends LazyLogging {

  @Autowired
  val httpServletRequest: HttpServletRequest = null

  @Autowired
  val spectrumPersistenceService: SpectrumPersistenceService = null

  @Autowired
  val submitterMongoRepository: SubmitterRepository = null

  @Autowired
  val loginService: LoginService = null



  /**
    * Executes a search against the repository and can cause out of memory errors.  It is recommended to utilize this
    * method with pagination
    *
    * @param rsqlQuery
    * @return
    */
  @RequestMapping(path = Array("/search"), method = Array(RequestMethod.GET), produces = Array("application/json", "text/msp"))
  @Async
  @ResponseBody
  def searchRSQL(@RequestParam(value = "page", required = false) page: Integer,
                 @RequestParam(value = "size", required = false) size: Integer,
                 @RequestParam(value = "query", required = false) rsqlQuery: WrappedString,
                 request: HttpServletRequest, response: HttpServletResponse): Future[ResponseEntity[Iterable[SpectrumResult]]] = {
    logger.info(s"BIGGER PENIS BUTT")
    def sendQuery(rsqlQuery: String, page: Integer, size: Integer): Iterable[SpectrumResult] = {

      if (size != null) {
        if (page != null) {
          val test = spectrumPersistenceService.findAll(rsqlQuery, PageRequest.of(page, size)).getContent.asScala
          logger.info(s"test var is class of${test.getClass}")
          test
        } else {
          val test = spectrumPersistenceService.findAll(rsqlQuery, PageRequest.of(0, size)).getContent.asScala
          logger.info(s"test var is class of ${test.getClass}")
          test
        }
      } else {
        val test = spectrumPersistenceService.findAll(rsqlQuery).asScala
        logger.info(s"test var is class of ${test.getClass}")
        test
      }
    }

    if (rsqlQuery != null) {
      val rsqlQueryString = if (rsqlQuery != null) rsqlQuery.string else ""

      new AsyncResult[ResponseEntity[Iterable[SpectrumResult]]](
        new ResponseEntity(sendQuery(rsqlQueryString, page, size), HttpStatus.OK)
      )
    } else {
      new AsyncResult[ResponseEntity[Iterable[SpectrumResult]]](new ResponseEntity(HttpStatus.BAD_REQUEST))
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
  def searchCount(@RequestParam(value = "query", required = false) rsqlQuery: WrappedString): Future[Long] = {
    logger.info(s"BIGGER PENIS BUTT")
    if ((rsqlQuery != null && rsqlQuery.string.nonEmpty)) {
      val rsqlQueryString = if (rsqlQuery != null) rsqlQuery.string else ""

      new AsyncResult[Long](spectrumPersistenceService.count(rsqlQueryString))
    } else {
      new AsyncResult[Long](spectrumPersistenceService.count())
    }
  }

  @RequestMapping(path = Array(""), method = Array(RequestMethod.DELETE))
  @Async
  @ResponseBody
  def massDeleteByID(@RequestBody ids: java.util.List[String]): Future[String] = {
    logger.info(s"BIGGER PENIS BUTT")
    spectrumPersistenceService.deleteSpectraByIdIn(ids)
    new AsyncResult[String]("Delete request received.")
  }

  @RequestMapping(path = Array("/search"), method = Array(RequestMethod.DELETE))
  @Async
  @ResponseBody
  def massDeleteBySearch(@RequestParam(value = "query", required = false) rsqlQuery: WrappedString): Future[String] = {
    logger.info(s"BIGGER PENIS BUTT")
    val rsqlQueryString = if (rsqlQuery != null) rsqlQuery.string else ""
    spectrumPersistenceService.deleteSpectraByQuery(rsqlQueryString)
    new AsyncResult[String]("Delete request received.")
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
  final def list(@RequestParam(value = "page", required = false) page: Integer, @RequestParam(value = "size", required = false) size: Integer): Future[ResponseEntity[Iterable[SpectrumResult]]] = {
    logger.info(s"BIGGER PENIS BUTT")
    doList(page, size)
  }

  def doList(page: Integer, size: Integer): Future[ResponseEntity[Iterable[SpectrumResult]]] = {
    val data: Iterable[SpectrumResult] = {
      if (size != null) {
        if (page != null) {
          spectrumPersistenceService.findAll(PageRequest.of(page, size, Sort.Direction.ASC, "id")).getContent.asScala
        } else {
          spectrumPersistenceService.findAll(PageRequest.of(0, size, Sort.Direction.ASC, "id")).getContent.asScala
        }
      } else {
        new DynamicIterable[SpectrumResult, String]("", 50) {
          // loads more data from the server for the given query
          override def fetchMoreData(query: String, pageable: Pageable): Page[SpectrumResult] = spectrumPersistenceService.findAll(pageable)
        }.asScala
      }
    }

    val headers = new HttpHeaders()
    // headers.add("Content-Type", servletRequest.getContentType)

    new AsyncResult[ResponseEntity[Iterable[SpectrumResult]]](
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
    logger.info(s"BIGGER PENIS BUTT")
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
  final def save(@Valid @RequestBody resource: SpectrumResult): Future[ResponseEntity[SpectrumResult]] = {
    logger.info(s"BIGGER PENIS BUTT")
    doSave(resource)
  }

  def finalSave(resource: SpectrumResult): Future[ResponseEntity[SpectrumResult]] = {
    new AsyncResult[ResponseEntity[SpectrumResult]](
      new ResponseEntity[SpectrumResult](spectrumPersistenceService.save(resource), HttpStatus.OK)
    )
  }

  /**
    * Saves a spectrum
    *
    * @param spectrum
    * @return
    */
  def doSave(spectrum: SpectrumResult): Future[ResponseEntity[SpectrumResult]] = {

    logger.info(s"BIG PENIS BUTT")
    val token: String = httpServletRequest.getHeader("Authorization").split(" ").last
    val loginInfo: LoginInfo = loginService.info(token)

    val existingSubmitter: Submitter = submitterMongoRepository.findByEmailAddress(loginInfo.emailAddress)

    // Admins can save anything
    if (loginInfo.roles.contains("ADMIN")) {
      if (spectrum.getMonaId == null || !spectrumPersistenceService.existsById(spectrum.getMonaId)) {
        finalSave(spectrum)
      } else {
        val existingSpectrum: SpectrumResult = spectrumPersistenceService.findByMonaId(spectrum.getMonaId)
        finalSave(spectrum)
      }
    }

    // If a user has no submitter information, we cannot accept the spectrum
    else if (existingSubmitter == null) {
      new AsyncResult[ResponseEntity[SpectrumResult]](new ResponseEntity[SpectrumResult](HttpStatus.FORBIDDEN))
    }

    // If no id is provided, a new record can be added with no issues
    else if (spectrum.getMonaId == null || spectrum.getMonaId.isEmpty) {
      val submitterDAO = new SubmitterDAO(existingSubmitter.getEmailAddress, existingSubmitter.getFirstName, existingSubmitter.getLastName, existingSubmitter.getInstitution)
      spectrum.getSpectrum.setId(null)
      spectrum.getSpectrum.setSubmitter(submitterDAO)
      doSave(spectrum)
    }

    // Check whether a spectrum with the given id exists.  If it does, the submitter
    // must own it to update it.  Otherwise, the request is not allowed
    else {
      val existingSpectrum: SpectrumResult = spectrumPersistenceService.findByMonaId(spectrum.getMonaId)

      if (existingSpectrum == null) {
        finalSave(spectrum)
      } else if (existingSpectrum.getSpectrum.getSubmitter.getEmailAddress == loginInfo.emailAddress) {
        val submitterDAO = new SubmitterDAO(existingSubmitter.getEmailAddress, existingSubmitter.getFirstName, existingSubmitter.getLastName, existingSubmitter.getInstitution)
        spectrum.getSpectrum.setDateCreated(existingSpectrum.getSpectrum.getDateCreated)
        spectrum.getSpectrum.setSubmitter(submitterDAO)
        finalSave(spectrum)
      } else {
        new AsyncResult[ResponseEntity[SpectrumResult]](new ResponseEntity[SpectrumResult](HttpStatus.CONFLICT))
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
  final def get(@PathVariable("id") id: String, servletRequest: ServletRequest, servletResponse: ServletResponse): Future[ResponseEntity[SpectrumResult]] = {
    logger.info(s"BIGGER PENIS BUTT")
    doGet(id, servletRequest, servletResponse)
  }

  def doGet(id: String, servletRequest: ServletRequest, servletResponse: ServletResponse): Future[ResponseEntity[SpectrumResult]] = {
    val headers = new HttpHeaders()
    // headers.add("Content-Type", servletRequest.getContentType)

    if (spectrumPersistenceService.existsById(id)) {
      new AsyncResult[ResponseEntity[SpectrumResult]](new ResponseEntity[SpectrumResult](spectrumPersistenceService.findByMonaId(id), headers, HttpStatus.OK))
    } else {
      new AsyncResult[ResponseEntity[SpectrumResult]](new ResponseEntity[SpectrumResult](HttpStatus.NOT_FOUND))
    }
  }


  /**
   * Removes the specified resource from the system
   *
   * @param id
   * @return
   */
  @Async
  @RequestMapping(path = Array("/{id}"), method = Array(RequestMethod.DELETE))
  @ResponseBody
  final def delete(@PathVariable("id") id: String): Unit = {
    logger.info(s"BIGGER PENIS BUTT")
    doDelete(id)
  }

  def doDelete(id: String): Unit = spectrumPersistenceService.deleteById(id)


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
  final def put(@PathVariable("id") id: String, @Valid @RequestBody resource: SpectrumResult): Future[ResponseEntity[SpectrumResult]] = {
    logger.info(s"BIGGER PENIS BUTT")
    doPut(id, resource)
  }

  /**
    * Saves the provided spectrum at the given path
    *
    * @param id
    * @param spectrum
    * @return
    */
  def doPut(id: String, spectrum: SpectrumResult): Future[ResponseEntity[SpectrumResult]] = {

    val token: String = httpServletRequest.getHeader("Authorization").split(" ").last
    val loginInfo: LoginInfo = loginService.info(token)

    val existingSubmitter: Submitter = submitterMongoRepository.findByEmailAddress(loginInfo.emailAddress)

    // Admins can save anything
    if (loginInfo.roles.contains("ADMIN")) {
      if (spectrum.getMonaId == null || spectrum.getMonaId == "" || spectrum.getMonaId == id) {
        if (spectrum.getMonaId != null && !spectrumPersistenceService.existsById(spectrum.getMonaId)) {
          spectrum.setMonaId(id)
          spectrum.getSpectrum.setId(id)
          finalSave(spectrum)
        } else {
          val existingOldSpectrum: SpectrumResult = spectrumPersistenceService.findByMonaId(spectrum.getMonaId)
          spectrum.setMonaId(id)
          spectrum.getSpectrum.setId(id)
          spectrum.getSpectrum.setDateCreated(existingOldSpectrum.getSpectrum.getDateCreated)
          finalSave(spectrum)
        }
      } else {
        val existingOldSpectrum: SpectrumResult = spectrumPersistenceService.findByMonaId(spectrum.getMonaId)
        val existingNewSpectrum: SpectrumResult = spectrumPersistenceService.findByMonaId(id)

        spectrumPersistenceService.deleteById(spectrum.getMonaId)

        if (existingOldSpectrum != null) {
          spectrum.setMonaId(id)
          spectrum.getSpectrum.setId(id)
          spectrum.getSpectrum.setDateCreated(existingOldSpectrum.getSpectrum.getDateCreated)
          finalSave(spectrum)
        } else if (existingNewSpectrum != null) {
          spectrum.setMonaId(id)
          spectrum.getSpectrum.setId(id)
          spectrum.getSpectrum.setDateCreated(existingNewSpectrum.getSpectrum.getDateCreated)
          finalSave(spectrum)
        } else {
          spectrum.setMonaId(id)
          spectrum.getSpectrum.setId(id)
          finalSave(spectrum)
        }
      }
    }

    // If a user has no submitter information, we cannot accept the spectrum
    else if (existingSubmitter == null) {
      new AsyncResult[ResponseEntity[SpectrumResult]](new ResponseEntity[SpectrumResult](HttpStatus.FORBIDDEN))
    }

    // User should be able to update or change the id their own spectra
    else {
      // Handle the case of saving a new spectrum/updating record $id
      if (spectrum.getMonaId == null || spectrum.getMonaId.isEmpty || spectrum.getMonaId == id) {
        if (spectrum.getMonaId != null && !spectrumPersistenceService.existsById(spectrum.getMonaId)) {
          val submitterDAO = new SubmitterDAO(existingSubmitter.getEmailAddress, existingSubmitter.getFirstName, existingSubmitter.getLastName, existingSubmitter.getInstitution)
          spectrum.setMonaId(id)
          spectrum.getSpectrum.setId(id)
          spectrum.getSpectrum.setSubmitter(submitterDAO)
          finalSave(spectrum)
        } else {
          val existingOldSpectrum: SpectrumResult = spectrumPersistenceService.findByMonaId(spectrum.getMonaId)

          if (existingOldSpectrum.getSpectrum.getSubmitter == null || existingOldSpectrum.getSpectrum.getSubmitter.getEmailAddress == loginInfo.emailAddress) {
            val submitterDAO = new SubmitterDAO(existingSubmitter.getEmailAddress, existingSubmitter.getFirstName, existingSubmitter.getLastName, existingSubmitter.getInstitution)
            spectrum.setMonaId(id)
            spectrum.getSpectrum.setId(id)
            spectrum.getSpectrum.setDateCreated(existingOldSpectrum.getSpectrum.getDateCreated)
            spectrum.getSpectrum.setSubmitter(submitterDAO)
            finalSave(spectrum)
          } else {
            new AsyncResult[ResponseEntity[SpectrumResult]](new ResponseEntity[SpectrumResult](HttpStatus.FORBIDDEN))
          }
        }
      }

      // Handle the case of differing ids
      else {
        val existingOldSpectrum: SpectrumResult = spectrumPersistenceService.findByMonaId(spectrum.getMonaId)
        val existingNewSpectrum: SpectrumResult = spectrumPersistenceService.findByMonaId(id)

        if (existingOldSpectrum != null && existingOldSpectrum.getSpectrum.getSubmitter.getEmailAddress != loginInfo.emailAddress) {
          // Not allowed to delete old spectrum if it belongs to someone else
          new AsyncResult[ResponseEntity[SpectrumResult]](new ResponseEntity[SpectrumResult](HttpStatus.CONFLICT))
        } else if (existingNewSpectrum != null && existingNewSpectrum.getSpectrum.getSubmitter.getEmailAddress != loginInfo.emailAddress) {
          // Not allowed to update the new spectrum if it belongs to someone else
          new AsyncResult[ResponseEntity[SpectrumResult]](new ResponseEntity[SpectrumResult](HttpStatus.CONFLICT))
        } else {
          spectrumPersistenceService.deleteById(spectrum.getMonaId)

          // Use the old dateCreated field
          if (existingOldSpectrum != null) {
            val submitterDAO = new SubmitterDAO(existingSubmitter.getEmailAddress, existingSubmitter.getFirstName, existingSubmitter.getLastName, existingSubmitter.getInstitution)
            spectrum.setMonaId(id)
            spectrum.getSpectrum.setId(id)
            spectrum.getSpectrum.setDateCreated(existingOldSpectrum.getSpectrum.getDateCreated)
            spectrum.getSpectrum.setSubmitter(submitterDAO)
            finalSave(spectrum)
          } else if (existingNewSpectrum != null) {
            val submitterDAO = new SubmitterDAO(existingSubmitter.getEmailAddress, existingSubmitter.getFirstName, existingSubmitter.getLastName, existingSubmitter.getInstitution)
            spectrum.setMonaId(id)
            spectrum.getSpectrum.setId(id)
            spectrum.getSpectrum.setDateCreated(existingNewSpectrum.getSpectrum.getDateCreated)
            spectrum.getSpectrum.setSubmitter(submitterDAO)
            finalSave(spectrum)
          } else {
            val submitterDAO = new SubmitterDAO(existingSubmitter.getEmailAddress, existingSubmitter.getFirstName, existingSubmitter.getLastName, existingSubmitter.getInstitution)
            spectrum.setMonaId(id)
            spectrum.getSpectrum.setId(id)
            spectrum.getSpectrum.setSubmitter(submitterDAO)
            finalSave(spectrum)
          }
        }
      }
    }
  }
}
