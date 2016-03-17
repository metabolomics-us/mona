package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller

import java.lang.Iterable
import java.util.concurrent.Future

import edu.ucdavis.fiehnlab.mona.backend.core.domain.HelperTypes.WrappedString
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.ISpectrumMongoRepositoryCustom
import edu.ucdavis.fiehnlab.mona.backend.core.service.persistence.SpectrumPersistenceService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.mongodb.core.query.BasicQuery
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.scheduling.annotation.{AsyncResult, Async}
import org.springframework.web.bind.annotation._
import scala.collection.JavaConverters._

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
  @RequestMapping(path = Array("/search"), method = Array(RequestMethod.GET))
  @Async
  def searchRSQL(@RequestParam(value = "page", required = false) page: Integer, @RequestParam(value = "size", required = false) size: Integer, @RequestParam(value = "query", required = true) query: WrappedString): Future[java.util.List[Spectrum]] = new AsyncResult[java.util.List[Spectrum]](
    if (size != null)
      if (page != null)
        spectrumPersistenceService.findAll(query.string, new PageRequest(page, size)).getContent
      else
        spectrumPersistenceService.findAll(query.string, new PageRequest(0, size)).getContent
    else
      spectrumPersistenceService.findAll(query.string).asScala.toList.asJava
  )


  /**
    * this method returns the counts of objects, which would be received by the given query
    *
    * @return
    */
  @RequestMapping(path = Array("/count"), method = Array(RequestMethod.POST))
  @Async
  def searchCount(@RequestBody query: WrappedString): Future[Long] = {
    new AsyncResult[Long](spectrumPersistenceService.count(query.string))
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
  override def put(@PathVariable("id") id: String, @RequestBody spectrum: Spectrum): Future[Spectrum] = {

    if (id == spectrum.id) {
      new AsyncResult[Spectrum](
        getRepository.save(spectrum.copy(id = id))
      )

    }
    else {
      getRepository.delete(spectrum.id)

      val newSpectrum = spectrum.copy(id = id)
      val result = getRepository.save(newSpectrum)

      new AsyncResult[Spectrum](
        result
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
