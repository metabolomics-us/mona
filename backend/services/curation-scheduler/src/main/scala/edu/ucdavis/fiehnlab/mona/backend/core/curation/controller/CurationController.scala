package edu.ucdavis.fiehnlab.mona.backend.core.curation.controller

import edu.ucdavis.fiehnlab.mona.backend.core.curation.service.CurrationService
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.util.DynamicIterable
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.ISpectrumMongoRepositoryCustom
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.{Page, Pageable}
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.scheduling.annotation.Async
import org.springframework.web.bind.annotation.{PathVariable, RequestMapping, RequestParam, RestController}

/**
  * This controller permits us to easily schedule the curation of spectra in the system
  * these calls will be internally forwarded to a queue, which will take care of the actual
  * execution. This allows the service to be very cheap and to easily scale
  */
@RestController
@RequestMapping(value = Array("/rest/curation"))
class CurationController {

  @Autowired
  val repository: ISpectrumMongoRepositoryCustom = null

  @Autowired
  val curationService: CurrationService = null

  /**
    * schedules the spectra with the specified id for curation
    *
    * @param id
    * @return
    */
  @RequestMapping(path = Array("/{id}"))
  @Async
  def curateById(@PathVariable("id") id: String) = {

    val spectrum = repository.findOne(id)

    if (spectrum == null) {
      new ResponseEntity[Spectrum](HttpStatus.NOT_FOUND)
    }
    else {
      curationService.scheduleSpectra(spectrum)
    }
  }

  /**
    * schedules every spectra for the given query for curation
    *
    * @param query
    */
  @RequestMapping(path = Array("/"))
  @Async
  def curateByQuery(@RequestParam(required = false, name = "query") query: String) = {
    if (query == null) {
      val iterator = repository.findAll().iterator()

      while (iterator.hasNext) {
        val spectrum = iterator.next()
        curationService.scheduleSpectra(spectrum)
      }
    }
    else {
      val iterable = new DynamicIterable[Spectrum, String](query, 10) {
        /**
          * loads more data from the server for the given query
          */
        override def fetchMoreData(query: String, pageable: Pageable): Page[Spectrum] = {
          repository.rsqlQuery(query, pageable)
        }
      }

      val iterator = iterable.iterator

      while (iterator.hasNext) {
        val spectrum = iterator.next()
        curationService.scheduleSpectra(spectrum)
      }
    }
  }

}
