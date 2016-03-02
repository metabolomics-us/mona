package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.controller

import java.lang.Iterable
import java.util.concurrent.Future

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.ISpectrumRepositoryCustom
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.scheduling.annotation.{AsyncResult, Async}
import org.springframework.web.bind.annotation._
import scala.collection.JavaConverters._

/**
  * This is the main spectrum related REST controller and should be utilized for most operations
  * interacting with the system.
  *
  * It allows you to perform the standard operations, you would like todo on a spectrum
  *
  */
@RestController
@RequestMapping(Array("/rest/spectra"))
class SpectrumRestController {

  /**
    * this is the utilized repository, doing all the heavy lifting
    */
  @Autowired
  val spectrumRepository: ISpectrumRepositoryCustom = null

  /**
    * this will return all the specified data in the system
    * please be aware that this can cause out of memory issues
    * and should be always utilized with pagination
    *
    * @return
    */
  @RequestMapping(path = Array(""), method = Array(RequestMethod.GET))
  @Async
  def listAllSpectra(@RequestParam(value = "page", required = false) page: Integer, @RequestParam(value = "size", required = false) size: Integer): Future[Iterable[Spectrum]] = new AsyncResult[Iterable[Spectrum]](
    if (size != null) {
      if (page != null) {
        spectrumRepository.findAll(new PageRequest(page, size)).getContent
      }
      else {
        spectrumRepository.findAll(new PageRequest(0, size)).getContent
      }
    }
    else {
      spectrumRepository.findAll()
    }
  )

  /**
    * this executes a search against the reposiroty and can cause out of memory errors. We recommend to utilize this method with
    * pagination as well
    *
    * @param query
    * @return
    */
  @RequestMapping(path = Array("/search"), method = Array(RequestMethod.POST))
  @Async
  def searchSpectra(@RequestParam(value = "page", required = false) page: Integer, @RequestParam(value = "size", required = false) size: Integer, @RequestBody query: String): Future[java.util.List[Spectrum]] = new AsyncResult[java.util.List[Spectrum]](
    if (size != null)
      if (page != null)
        spectrumRepository.executeQuery(query, new PageRequest(page, size)).getContent
      else
        spectrumRepository.executeQuery(query, new PageRequest(0, size)).getContent
    else
      spectrumRepository.executeQuery(query)
  )

  /**
    * this method returns the counts of objects, which would be received by the given query
    *
    * @return
    */
  @RequestMapping(path = Array("/count"), method = Array(RequestMethod.POST))
  @Async
  def searchSpectraCount(@RequestBody query: String): Future[Long] = {
    new AsyncResult[Long](spectrumRepository.executeQueryCount(query))
  }

  /**
    * this method returns the complete count of spectra in the system
    *
    * @return
    */
  @RequestMapping(path = Array("/count"), method = Array(RequestMethod.GET))
  @Async
  def searchSpectraCount: Future[Long] = {
    new AsyncResult[Long](spectrumRepository.count())
  }


  /**
    * saves a spectra or updates it. This will depend on the utilized repository
    *
    * @param spectrum
    * @return
    */
  @Async
  @RequestMapping(path = Array(""), method = Array(RequestMethod.POST))
  def save(@RequestBody spectrum: Spectrum) = new AsyncResult[Spectrum](
    spectrumRepository.save(spectrum)
  )

  /**
    * looks for the exact spectra
    *
    * @param spectrum
    * @return
    */
  @Async
  @RequestMapping(path = Array("/{id}"), method = Array(RequestMethod.GET))
  def get(@PathVariable("id") spectrum: String): Future[ResponseEntity[Spectrum]] = {

    if (spectrumRepository.exists(spectrum)) {
      new AsyncResult[ResponseEntity[Spectrum]](
        new ResponseEntity[Spectrum](spectrumRepository.findOne(spectrum), HttpStatus.OK)
      )
    }
    else {
      new AsyncResult[ResponseEntity[Spectrum]](
        new ResponseEntity[Spectrum](HttpStatus.NOT_FOUND)
      )
    }
  }


  /**
    * this methods removes the specified method from the system
    *
    * @param spectrum
    * @return
    */
  @Async
  @RequestMapping(path = Array("/{id}"), method = Array(RequestMethod.DELETE))
  def delete(@PathVariable("id") spectrum: String) = spectrumRepository.delete(spectrum)

  /**
    * saves the provided spectrum at the given path
    *
    * @param id
    * @param spectrum
    * @return
    */
  @Async
  @RequestMapping(path = Array("/{id}"), method = Array(RequestMethod.PUT))
  def put(@PathVariable("id") id: String, @RequestBody spectrum: Spectrum): Future[Spectrum] = {

    if (id == spectrum.id) {
      new AsyncResult[Spectrum](
        spectrumRepository.save(spectrum.copy(id = id))
      )

    }
    else {
      spectrumRepository.delete(spectrum.id)

      val newSpectrum = spectrum.copy(id = id)
      val result = spectrumRepository.save(newSpectrum)

      new AsyncResult[Spectrum](
        result
      )
    }
  }

}
