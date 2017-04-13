package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.spectrum

import java.util.concurrent.Future
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import edu.ucdavis.fiehnlab.mona.backend.core.domain.HelperTypes.{LoginInfo, WrappedString}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Spectrum, Submitter}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.ISubmitterMongoRepository
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.GenericRESTController
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.service.persistence.SpectrumPersistenceService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.{PageRequest, Sort}
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.scheduling.annotation.{Async, AsyncResult}
import org.springframework.web.bind.annotation.{RequestMapping, _}

import scala.collection.JavaConverters._

@CrossOrigin
@RestController
@RequestMapping(Array("/rest/spectra"))
class SpectrumRestController extends GenericRESTController[Spectrum] {

  @Autowired
  val httpServletRequest: HttpServletRequest = null

  @Autowired
  val spectrumPersistenceService: SpectrumPersistenceService = null

  @Autowired
  val submitterMongoRepository: ISubmitterMongoRepository = null

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
                 @RequestParam(value = "text", required = false) text: WrappedString,
                 request: HttpServletRequest, response: HttpServletResponse): Future[ResponseEntity[Iterable[Spectrum]]] = {

    def sendQuery(query: String, page: Integer, size: Integer, rsqlQuery: Boolean): Iterable[Spectrum] = {
      if (size != null) {
        if (page != null) {
          spectrumPersistenceService.findAll(query, rsqlQuery, new PageRequest(page, size, Sort.Direction.ASC, "id")).getContent.asScala
        } else {
          spectrumPersistenceService.findAll(query, rsqlQuery, new PageRequest(0, size, Sort.Direction.ASC, "id")).getContent.asScala
        }
      } else {
        spectrumPersistenceService.findAll(query, rsqlQuery).asScala
      }
    }

    // Handle RSQL query
    if (query != null && query.string != "") {
      new AsyncResult[ResponseEntity[Iterable[Spectrum]]](
        new ResponseEntity(sendQuery(query.string, page, size, rsqlQuery = true), HttpStatus.OK)
      )
    }

    // Handle full text query
    else if (text != null && text.string != "") {
      new AsyncResult[ResponseEntity[Iterable[Spectrum]]](
        new ResponseEntity(sendQuery(text.string, page, size, rsqlQuery = false), HttpStatus.OK)
      )
    }

    // Otherwse, 400 error
    else {
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
  def searchCount(@RequestParam(value = "query", required = false) query: WrappedString,
                  @RequestParam(value = "text", required = false) text: WrappedString): Future[Long] = {

    if (query != null && query.string != "") {
      new AsyncResult[Long](spectrumPersistenceService.count(query.string))
    } else if (text != null && text.string != "") {
      new AsyncResult[Long](spectrumPersistenceService.count(query.string, isRSQLQuery = false))
    } else {
      new AsyncResult[Long](spectrumPersistenceService.count())
    }
  }


  /**
    * Saves a spectrum
    *
    * @param spectrum
    * @return
    */
  override def doSave(spectrum: Spectrum): Future[ResponseEntity[Spectrum]] = {

    val token: String = httpServletRequest.getHeader("Authorization").split(" ").last
    val loginInfo: LoginInfo = loginService.info(token)

    val existingSubmitter: Submitter = submitterMongoRepository.findById(loginInfo.username)

    // Admins can save anything
    if(loginInfo.roles.contains("ADMIN")) {
      super.doSave(spectrum)
    }

    // If a user has no submitter information, we cannot accept the spectrum
    else if (existingSubmitter == null) {
      new AsyncResult[ResponseEntity[Spectrum]](new ResponseEntity[Spectrum](HttpStatus.FORBIDDEN))
    }

    // If no id is provided, a new record can be added with no issues
    else if (spectrum.id == null || spectrum.id == "") {
      super.doSave(spectrum.copy(id = null, submitter = existingSubmitter))
    }

    // Check whether a spectrum with the given id exists.  If it does, the submitter
    // must own it to update it.  Otherwise, the request is not allowed
    else {
      val existingSpectrum: Spectrum = getRepository.findOne(spectrum.id)

      if (existingSpectrum == null || existingSpectrum.submitter.id == loginInfo.username) {
        super.doSave(spectrum.copy(submitter = existingSubmitter))
      } else {
        new AsyncResult[ResponseEntity[Spectrum]](new ResponseEntity[Spectrum](HttpStatus.CONFLICT))
      }
    }
  }


  /**
    * Saves the provided spectrum at the given path
    *
    * @param id
    * @param spectrum
    * @return
    */
  override def doPut(id: String, spectrum: Spectrum): Future[ResponseEntity[Spectrum]] = {

    val token: String = httpServletRequest.getHeader("Authorization").split(" ").last
    val loginInfo: LoginInfo = loginService.info(token)

    val existingSubmitter: Submitter = submitterMongoRepository.findById(loginInfo.username)

    // Admins can save anything
    if (loginInfo.roles.contains("ADMIN")) {
      if (spectrum.id == null || spectrum.id == "" || spectrum.id == id) {
        super.doSave(spectrum.copy(id = id))
      } else {
        getRepository.delete(spectrum.id)
        super.doSave(spectrum.copy(id = id))
      }
    }

    // If a user has no submitter information, we cannot accept the spectrum
    else if (existingSubmitter == null) {
      new AsyncResult[ResponseEntity[Spectrum]](new ResponseEntity[Spectrum](HttpStatus.FORBIDDEN))
    }

    // User should be able to update or change the id their own spectra
    else {
      // Handle the case of saving a new spectrum/updating record $id
      if (spectrum.id == null || spectrum.id == "" || spectrum.id == id) {
        val existingSpectrum: Spectrum = getRepository.findOne(id)

        if (existingSpectrum == null || existingSpectrum.submitter.id == loginInfo.username) {
          super.doSave(spectrum.copy(id = id, submitter = existingSubmitter))
        } else {
          new AsyncResult[ResponseEntity[Spectrum]](new ResponseEntity[Spectrum](HttpStatus.FORBIDDEN))
        }
      }

      // Handle the case of differing ids
      else {
        val existingOldSpectrum: Spectrum = getRepository.findOne(spectrum.id)
        val existingNewSpectrum: Spectrum = getRepository.findOne(id)

        if (existingOldSpectrum != null && existingOldSpectrum.submitter.id != loginInfo.username) {
          // Not allowed to delete old spectrum if it belongs to someone else
          new AsyncResult[ResponseEntity[Spectrum]](new ResponseEntity[Spectrum](HttpStatus.CONFLICT))
        } else if (existingNewSpectrum != null && existingNewSpectrum.submitter.id != loginInfo.username) {
          // Not allowed to update the new spectrum if it belongs to someone else
          new AsyncResult[ResponseEntity[Spectrum]](new ResponseEntity[Spectrum](HttpStatus.CONFLICT))
        } else {
          getRepository.delete(spectrum.id)
          super.doSave(spectrum.copy(id = id, submitter = existingSubmitter))
        }
      }
    }
  }


  /**
    * Utilized repository
    *
    * @return
    */
  override def getRepository: PagingAndSortingRepository[Spectrum, String] = spectrumPersistenceService
}
