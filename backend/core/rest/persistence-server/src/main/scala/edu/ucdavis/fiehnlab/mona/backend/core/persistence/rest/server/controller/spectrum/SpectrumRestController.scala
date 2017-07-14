package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.spectrum

import java.util.concurrent.Future
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import edu.ucdavis.fiehnlab.mona.backend.core.domain.HelperTypes.{LoginInfo, WrappedString}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Spectrum, Submitter}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.{BlacklistedSplashMongoRepository, ISubmitterMongoRepository}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.GenericRESTController
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.service.persistence.SpectrumPersistenceService
import edu.ucdavis.fiehnlab.spectra.hash.core.types.SpectraType
import edu.ucdavis.fiehnlab.spectra.hash.core.util.SplashUtil
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
  val blacklistedSplashRepository: BlacklistedSplashMongoRepository = null

  @Autowired
  val loginService: LoginService = null


  /**
    * Validate the given spectrum by verifying that its SPLASH is not blacklisted
    * @param spectrum
    * @return
    */
  private def validateSpectrum(spectrum: String): Boolean = {
    val splash: String = SplashUtil.splash(spectrum, SpectraType.MS)

    !blacklistedSplashRepository.exists(splash)
  }


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
                 @RequestParam(value = "text", required = false) textQuery: WrappedString,
                 request: HttpServletRequest, response: HttpServletResponse): Future[ResponseEntity[Iterable[Spectrum]]] = {

    def sendQuery(rsqlQuery: String, textQuery: String, page: Integer, size: Integer): Iterable[Spectrum] = {
      if (size != null) {
        if (page != null) {
          spectrumPersistenceService.findAll(rsqlQuery, textQuery, new PageRequest(page, size, Sort.Direction.ASC, "id")).getContent.asScala
        } else {
          spectrumPersistenceService.findAll(rsqlQuery, textQuery, new PageRequest(0, size, Sort.Direction.ASC, "id")).getContent.asScala
        }
      } else {
        spectrumPersistenceService.findAll(rsqlQuery, textQuery).asScala
      }
    }

    if (rsqlQuery != null || textQuery != null) {
      val rsqlQueryString = if (rsqlQuery != null) rsqlQuery.string else ""
      val textQueryString = if (textQuery != null) textQuery.string else ""

      new AsyncResult[ResponseEntity[Iterable[Spectrum]]](
        new ResponseEntity(sendQuery(rsqlQueryString, textQueryString, page, size), HttpStatus.OK)
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
  def searchCount(@RequestParam(value = "query", required = false) rsqlQuery: WrappedString,
                  @RequestParam(value = "text", required = false) textQuery: WrappedString): Future[Long] = {

    if ((rsqlQuery != null && rsqlQuery.string.nonEmpty) || (textQuery != null && textQuery.string.nonEmpty)) {
      val rsqlQueryString = if (rsqlQuery != null) rsqlQuery.string else ""
      val textQueryString = if (textQuery != null) textQuery.string else ""

      new AsyncResult[Long](spectrumPersistenceService.count(rsqlQueryString, textQueryString))
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

    // Return a 422 Unprocessable Entity error if the spectrum is not valid
    if (!validateSpectrum(spectrum.spectrum)) {
      new AsyncResult[ResponseEntity[Spectrum]](new ResponseEntity[Spectrum](HttpStatus.UNPROCESSABLE_ENTITY))
    }

    // Admins can save anything
    else if(loginInfo.roles.contains("ADMIN")) {
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

    // Return a 422 Unprocessable Entity error if the spectrum is not valid
    if (!validateSpectrum(spectrum.spectrum)) {
      new AsyncResult[ResponseEntity[Spectrum]](new ResponseEntity[Spectrum](HttpStatus.UNPROCESSABLE_ENTITY))
    }

    // Admins can save anything
    else if(loginInfo.roles.contains("ADMIN")) {
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
