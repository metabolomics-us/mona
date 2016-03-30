package edu.ucdavis.fiehnlab.mona.backend.curation.writer

import javax.annotation.PostConstruct

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.api.MonaSpectrumRestClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpServerErrorException

/**
  * A writer to persiste values to the repository, it has a silent retry feature and will attempt to retry saving the result
  * in case of error N times before it surrenders and throws an exceptions
  */
@Component
class RestRepositoryWriter(val loginToken:String, val retrySilently:Boolean = true, val maxRetries:Int = 10) extends WriterAdapter with LazyLogging {

  @Autowired
  val monaSpectrumRestClient: MonaSpectrumRestClient = null

  @PostConstruct
  def authorize = {
    logger.debug("logging in to server")
    monaSpectrumRestClient.login(loginToken)
  }

  private var counter:Int = 0

  /**
    * how many tries are left for the current data set
    */
  private var retriesLeft = maxRetries


  /**
    * attempts to write all these spectra to the repository
    * and saves them if they have an id or updates them if they do not have an id
    *
    * @param spectrum
    */
  override def write(spectrum: Spectrum): Unit = {

    try {
      if (spectrum.id == null) {
        logger.debug("adding spectra to server")
        monaSpectrumRestClient.add(spectrum)
      }
      else {
        logger.debug(s"updating spectra on server ${spectrum.id}")

        val s = monaSpectrumRestClient.get(spectrum.id)

        if (s != null) {
          monaSpectrumRestClient.updateAsync(spectrum, spectrum.id)
        }
        else {
          logger.debug("server was not aware of id, assuming it's a backup and adding it instead")
          monaSpectrumRestClient.add(spectrum)
        }

      }

      counter = counter + 1

      if (counter % 1000 == 1) {
        logger.info(s"written ${counter} spectra to the repository")
      }
    }
    catch {
      case e:HttpServerErrorException =>
        logger.warn(e.getMessage)

        if(retrySilently && retriesLeft > 0){
          retriesLeft = retriesLeft -1
          logger.debug(s"attempting recovery ${maxRetries - retriesLeft} out of ${maxRetries}")

          Thread.currentThread().wait(1000)
          write(spectrum)

          //success time to reset the retires
          retriesLeft = maxRetries
        }
        else{
          throw e
        }
    }
  }
}
