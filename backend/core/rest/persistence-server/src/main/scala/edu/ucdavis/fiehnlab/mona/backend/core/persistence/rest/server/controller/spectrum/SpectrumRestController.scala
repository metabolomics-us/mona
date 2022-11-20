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
import edu.ucdavis.fiehnlab.mona.backend.core.domain.SpectrumSubmitter
import edu.ucdavis.fiehnlab.mona.backend.core.domain.util.DynamicIterable
import edu.ucdavis.fiehnlab.mona.backend.core.domain.dao.SubmitterDAO
import edu.ucdavis.fiehnlab.mona.backend.core.domain.dao.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.SubmitterRepository
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

  @RequestMapping(path = Array(""), method = Array(RequestMethod.DELETE))
  @Async
  @ResponseBody
  def massDeleteByID(@RequestBody ids: java.util.List[String]): Future[String] = {
    spectrumPersistenceService.deleteSpectraByIdIn(ids)
    new AsyncResult[String]("Delete request received.")
  }

  @RequestMapping(path = Array("/search"), method = Array(RequestMethod.DELETE))
  @Async
  @ResponseBody
  def massDeleteBySearch(@RequestParam(value = "query", required = false) query: WrappedString): Future[String] = {
    val queryString = if (query != null) query.string else ""
    spectrumPersistenceService.deleteSpectraByQuery(queryString)
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

    val existingSubmitter: SubmitterDAO = submitterRepository.findTopByEmailAddress(loginInfo.emailAddress)

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
  @Async
  @RequestMapping(path = Array("/{id}"), method = Array(RequestMethod.DELETE))
  @ResponseBody
  final def delete(@PathVariable("id") id: String): Unit = {
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
    val existingSubmitter: SubmitterDAO = submitterRepository.findTopByEmailAddress(loginInfo.emailAddress)

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
