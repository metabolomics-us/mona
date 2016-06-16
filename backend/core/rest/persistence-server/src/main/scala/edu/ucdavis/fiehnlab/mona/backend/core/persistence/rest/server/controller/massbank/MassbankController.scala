package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.massbank

import java.util.concurrent.Future

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.io.massbank.MassBankToSpectrumMapper
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.ISubmitterMongoRepository
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.service.persistence.SpectrumPersistenceService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.{Async, AsyncResult}
import org.springframework.web.bind.annotation._

import scala.io.Source
import scala.util.{Failure, Success, Try}

/**
  * Created by wohlgemuth on 5/12/16.
  */
@CrossOrigin
@RestController
@RequestMapping(Array("/rest/upload/massbank"))
class MassbankController extends LazyLogging {

  @Autowired
  val spectrumPersistenceService: SpectrumPersistenceService = null

  @Autowired
  val submitterMongoRepository: ISubmitterMongoRepository = null

  @RequestMapping(path = Array(""), method = Array(RequestMethod.POST))
  @Async
  def submit(@RequestHeader("Authorization") token: String, @RequestBody content: String): Future[Spectrum] = {
    val src: Source = Source.fromString(content)
    val result: Try[Spectrum] = MassBankToSpectrumMapper.parse(src)

    result match {
      case Success(spectrum) =>
        val spectra = spectrumPersistenceService.save(spectrum)
        new AsyncResult[Spectrum](spectra)

      case Failure(e) =>
        //logger.info(s"error parsing content: ${e.getMessage}",e)
        throw e
    }
  }
}
