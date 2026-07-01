package edu.ucdavis.fiehnlab.mona.backend.curation.writer

import javax.annotation.PostConstruct
import java.util.concurrent.atomic.AtomicInteger

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.api.MonaSpectrumRestClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.client.{HttpClientErrorException, HttpServerErrorException}

/**
  * A writer to persiste values to the repository, it has a silent retry feature and will attempt to retry saving the result
  * in case of error N times before it surrenders and throws an exceptions
  */
@Component
class RestRepositoryWriter(val loginToken: String, val retrySilently: Boolean = true, val maxRetries: Int = 50, val recoveryPauseInMS: Long = 5000) extends WriterAdapter with LazyLogging {

  @Autowired
  val monaSpectrumRestClient: MonaSpectrumRestClient = null

  @PostConstruct
  def authorize(): Unit = {
    logger.debug("logging in to server")
    monaSpectrumRestClient.login(loginToken)
  }

  private val counter: AtomicInteger = new AtomicInteger(0)

  /**
    * attempts to write all these spectra to the repository
    * and saves them if they have an id or updates them if they do not have an id
    *
    * @param spectrum
    */
  override def write(spectrum: Spectrum): Unit = writeWithRetries(spectrum, maxRetries)

  /**
    * Persists a single spectrum, retrying on server errors. Retries are carried as a method parameter so the
    * retry budget is per spectrum, letting multiple consumer threads write concurrently without sharing state
    *
    * @param spectrum
    * @param retriesLeft how many attempts remain for this spectrum
    */
  private def writeWithRetries(spectrum: Spectrum, retriesLeft: Int): Unit = {

    try {
      if (spectrum.getId == null) {
        logger.debug("adding spectra to server")
        monaSpectrumRestClient.add(spectrum)
      } else {
        logger.debug(s"${spectrum.getId}: updating spectra on server")

        try {
          val s = monaSpectrumRestClient.get(spectrum.getId)
          monaSpectrumRestClient.updateAsync(spectrum, spectrum.getId)
        } catch {
          case e: HttpClientErrorException =>
            if (e.getMessage.contains("404")) {
              logger.debug("server was not aware of id, assuming it's a backup and adding it instead")
              monaSpectrumRestClient.add(spectrum)
            } else {
              throw e
            }
        }
      }

      val written = counter.incrementAndGet()

      if (written % 1000 == 1) {
        logger.info(s"written $written spectra to the repository")
      }
    } catch {
      case e: HttpServerErrorException =>
        if (retrySilently && retriesLeft > 0) {
          logger.warn(s"${e.getMessage} attempting recovery ${maxRetries - retriesLeft + 1} out of $maxRetries")

          Thread.sleep(recoveryPauseInMS)
          writeWithRetries(spectrum, retriesLeft - 1)
        } else {
          throw e
        }
    }
  }
}
