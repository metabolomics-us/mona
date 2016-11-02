package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.spectrum

import java.util.concurrent.Future
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import edu.ucdavis.fiehnlab.mona.backend.core.domain.HelperTypes.WrappedString
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.GenericRESTController
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.service.persistence.SpectrumPersistenceService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.http.{HttpHeaders, HttpStatus, MediaType, ResponseEntity}
import org.springframework.scheduling.annotation.{Async, AsyncResult}
import org.springframework.web.bind.annotation.{RequestMapping, _}

import scala.collection.JavaConverters._

@CrossOrigin
@RestController
@RequestMapping(Array("/rest/spectra"))
class SpectrumRestController extends GenericRESTController[Spectrum] {

  /**
    * this is the utilized repository, doing all the heavy lifting
    */
  @Autowired
  val spectrumPersistenceService: SpectrumPersistenceService = null

  /**
    * this executes a search against the reposiroty and can cause out of memory errors. We recommend to utilize this method with
    * pagination as well
    *
    * @param query
    * @return
    */
  @RequestMapping(path = Array("/search"), method = Array(RequestMethod.GET), produces = Array("application/json","text/msp"))
  @Async
  @ResponseBody
  def searchRSQL(@RequestParam(value = "page", required = false) page: Integer, @RequestParam(value = "size", required = false) size: Integer, @RequestParam(value = "query", required = true) query: WrappedString, request: HttpServletRequest, response: HttpServletResponse): Future[ResponseEntity[Iterable[Spectrum]]] = {
    if (query == null || query.string == "") {
      list(page, size)
    } else {
      val data: Iterable[Spectrum] = {
        if (size != null) {
          if (page != null) {
            spectrumPersistenceService.findAll(query.string, new PageRequest(page, size)).getContent.asScala
          }
          else {
            spectrumPersistenceService.findAll(query.string, new PageRequest(0, size)).getContent.asScala
          }
        }
        else {
          spectrumPersistenceService.findAll(query.string).asScala
        }
      }

      new AsyncResult[ResponseEntity[Iterable[Spectrum]]](
        new ResponseEntity(
          data, HttpStatus.OK
        )
      )
    }
  }

  /**
    * this method returns the counts of objects, which would be received by the given query
    *
    * @return
    */
  @RequestMapping(path = Array("/search/count"), method = Array(RequestMethod.GET))
  @Async
  @ResponseBody
  def searchCount(@RequestParam(value = "query", required = false) query: WrappedString): Future[Long] = {
    if (query == null || query.string.isEmpty) {
      new AsyncResult[Long](spectrumPersistenceService.count())
    } else {
      new AsyncResult[Long](spectrumPersistenceService.count(query.string))
    }
  }


  /**
    * saves the provided spectrum at the given path
    *
    * @param id
    * @param spectrum
    * @return
    */
  @Async
  @RequestMapping(path = Array("/{id}"), method = Array(RequestMethod.PUT))
  @ResponseBody
  override def put(@PathVariable("id") id: String, @RequestBody spectrum: Spectrum): Future[ResponseEntity[Spectrum]] = {
    if (id == spectrum.id) {
      new AsyncResult(
        new ResponseEntity[Spectrum](
          spectrumPersistenceService.update(spectrum.copy(id = id)),
          HttpStatus.OK
        )
      )
    } else {
      getRepository.delete(spectrum.id)

      val newSpectrum = spectrum.copy(id = id)
      val result = getRepository.save(newSpectrum)

      new AsyncResult(
        new ResponseEntity(
          result,
          HttpStatus.OK
        )
      )
    }
  }

  /**
    * utilized repository
    *
    * @return
    */
  override def getRepository: PagingAndSortingRepository[Spectrum, String] = spectrumPersistenceService
}
