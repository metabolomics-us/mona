package edu.ucdavis.fiehnlab.mona.backend.core.curation.controller

import java.util.concurrent.Future
import javax.servlet.http.HttpServletRequest
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.curation.service.CurationService
import edu.ucdavis.fiehnlab.mona.backend.core.domain.dao.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.util.DynamicIterable
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.service.SpectrumPersistenceService
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.{Page, Pageable}
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.scheduling.annotation.{Async, AsyncResult}
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation._


/**
  * This controller permits us to easily schedule the curation of spectra in the system
  * these calls will be internally forwarded to a queue, which will take care of the actual
  * execution. This allows the service to be very cheap and to easily scale
  */
@RestController
@RequestMapping(value = Array("/rest/curation"))
class CurationController extends LazyLogging {
  @Autowired
  val spectrumPersistenceService: SpectrumPersistenceService = null

  @Autowired
  val curationService: CurationService = null

  /**
    * schedules the spectra with the specified id for curation
    *
    * @param id
    * @return
    */
  @RequestMapping(path = Array("/{id}"))
  @Async
  def curateById(@PathVariable("id") id: String, request: HttpServletRequest): Future[ResponseEntity[CurationJobScheduled]] = {
    val spectrum = spectrumPersistenceService.findByMonaId(id)

    if (spectrum == null) {
      new AsyncResult[ResponseEntity[CurationJobScheduled]](new ResponseEntity(HttpStatus.NOT_FOUND))
    } else {
      curationService.scheduleSpectrum(spectrum)

      new AsyncResult[ResponseEntity[CurationJobScheduled]](
        new ResponseEntity[CurationJobScheduled](CurationJobScheduled(1), HttpStatus.OK)
      )
    }
  }

  /**
    * schedules every spectra for the given query for curation
    *
    * @param query
    */
  @RequestMapping(path = Array(""))
  @Async
  def curateByQuery(@RequestParam(required = false, name = "query") query: String): Future[ResponseEntity[CurationJobScheduled]] = {

    val it = new DynamicIterable[Spectrum, String](query, 100) {
      /**
        * Loads more data from the server for the given query
        */
      override def fetchMoreData(query: String, pageable: Pageable): Page[Spectrum] = {
        if (query == null || query.isEmpty) {
          spectrumPersistenceService.findAll(pageable)
        } else {
          spectrumPersistenceService.findAll(query, pageable)
        }
      }
    }.iterator

    var count: Int = 0

    while (it.hasNext) {
      val spectrum = it.next()
      curationService.scheduleSpectrum(spectrum)
      count += 1

      if (count % 10000 == 0) {
        logger.info(s"Scheduled $count spectra...")
      }
    }

    logger.info(s"Finished scheduling $count spectra")
    new AsyncResult[ResponseEntity[CurationJobScheduled]](
      new ResponseEntity[CurationJobScheduled](CurationJobScheduled(count), HttpStatus.OK)
    )
  }

  /**
    * Curate a single spectrum on demand
    */
  @RequestMapping(path = Array(""), method = Array(RequestMethod.POST))
  def curateSpectrum(@RequestBody spectrum: Spectrum): Future[ResponseEntity[Spectrum]] = {
    new AsyncResult[ResponseEntity[Spectrum]](
      new ResponseEntity(curationService.curateSpectrum(spectrum), HttpStatus.OK)
    )
  }
}

@Schema
case class CurationJobScheduled(count: Int)
