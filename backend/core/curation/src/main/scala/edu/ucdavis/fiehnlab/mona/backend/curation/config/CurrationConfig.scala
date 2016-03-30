package edu.ucdavis.fiehnlab.mona.backend.curation.config

import java.io.{BufferedInputStream, File, FileInputStream, FileNotFoundException}

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.LinearWorkflow
import edu.ucdavis.fiehnlab.mona.backend.curation.reader.JSONFileSpectraReader
import edu.ucdavis.fiehnlab.mona.backend.curation.writer.RestRepositoryWriter
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.{ItemProcessor, ItemReader, ItemWriter}
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.{Bean, Configuration}

/**
  * defines a handful of different beans to simplify work with curration tasks
  * and assorted issues
  */
@Configuration
class CurrationConfig extends LazyLogging {


  /**
    * This defines the spectra curration workflow processor bean
    * and configures it for our use
    *
    * @return
    */
  @Bean
  def currationWorkflow: ItemProcessor[Spectrum, Spectrum] = {
    new LinearWorkflow[Spectrum](name = "spectra-curration")
  }

  /**
    * should be utilized in jobs to persist entities at the mona backend
    *
    * @return
    */
  @Bean
  @StepScope
  def restRepositoryWriter(@Value("#{jobParameters[loginToken]}")
                           loginToken: String): ItemWriter[Spectrum] = {
    new RestRepositoryWriter(loginToken)
  }

  /**
    * this bean provides us with an convinient way read large amount of spectral data from
    * a json file source
    *
    * @param file
    * @return
    */
  @Bean
  @StepScope
  def jsonFileReader(@Value("#{jobParameters[pathToFile]}")
                     file: String): ItemReader[Spectrum] = {

    if(file == null){
      throw new FileNotFoundException("you need to provide a file name, but instead the parameter was null!")
    }
    val reader = new JSONFileSpectraReader()

    if (new File(file).exists()) {
      logger.debug("a file was provided")
      reader.stream = new BufferedInputStream(new FileInputStream(file))
    }
    else {
      logger.warn(s"provided file ${file} did not exist, trying to load from classpath")
      reader.stream = getClass.getResourceAsStream(file)
    }

    reader
  }

}
