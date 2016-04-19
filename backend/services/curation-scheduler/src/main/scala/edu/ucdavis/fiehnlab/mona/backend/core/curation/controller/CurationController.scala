package edu.ucdavis.fiehnlab.mona.backend.core.curation.controller

import java.util.concurrent.Future
import javax.servlet.http.HttpServletRequest

import edu.ucdavis.fiehnlab.mona.backend.core.curation.service.CurationService
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.util.DynamicIterable
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.ISpectrumMongoRepositoryCustom
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.{Page, Pageable}
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.scheduling.annotation.{Async, AsyncResult}
import org.springframework.web.bind.annotation.{PathVariable, RequestMapping, RequestParam, RestController}
import org.springframework.web.servlet.mvc.multiaction.NoSuchRequestHandlingMethodException

import scala.collection.JavaConverters._


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
  val curationService: CurationService = null

  /**
    * schedules the spectra with the specified id for curation
    *
    * @param id
    * @return
    */
  @RequestMapping(path = Array("/{id}"))
  @Async
  def curateById(@PathVariable("id") id: String,request:HttpServletRequest) :Future[CurationJobScheduled]= {

    val spectrum = repository.findOne(id)

    if (spectrum == null) {
      throw new NoSuchRequestHandlingMethodException(request)
    }
    else {
      curationService.scheduleSpectra(spectrum)
      new AsyncResult[CurationJobScheduled](CurationJobScheduled(1))
    }
  }

  /**
    * schedules every spectra for the given query for curation
    *
    * @param query
    */
  @RequestMapping(path = Array(""))
  @Async
  def curateByQuery(@RequestParam(required = false, name = "query") query: String) :Future[CurationJobScheduled]= {
    if (query == null) {

      val count:Int = repository.findAll().asScala.foldLeft(0){(sum,spectrum:Spectrum) =>
        curationService.scheduleSpectra(spectrum)
        sum + 1
      }

      new AsyncResult[CurationJobScheduled](CurationJobScheduled(count))
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

      val count:Int = iterable.asScala.foldLeft(0){(sum,spectrum:Spectrum) =>
        curationService.scheduleSpectra(spectrum)
        sum + 1
      }

      new AsyncResult[CurationJobScheduled](CurationJobScheduled(count))
    }
  }

}


case class CurationJobScheduled(count:Int)