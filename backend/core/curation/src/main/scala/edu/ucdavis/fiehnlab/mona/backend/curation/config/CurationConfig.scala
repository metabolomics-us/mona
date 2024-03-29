package edu.ucdavis.fiehnlab.mona.backend.curation.config

import java.io.{BufferedInputStream, File, FileInputStream, FileNotFoundException}
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.config.BusConfig
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.{Workflow, WorkflowBuilder}
import edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound.CalculateCompoundProperties
import edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound.adduct.AdductPrediction
import edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound.classyfire.ClassyfireProcessor
import edu.ucdavis.fiehnlab.mona.backend.curation.processor.instrument.IdentifyChromatography
import edu.ucdavis.fiehnlab.mona.backend.curation.processor.metadata._
import edu.ucdavis.fiehnlab.mona.backend.curation.processor.spectrum.{CalculateMassAccuracy, CalculateSplash, NormalizeSpectrum, SpectrumIonCountScoringRule}
import edu.ucdavis.fiehnlab.mona.backend.curation.processor.validation.MassAccuracyValidation
import edu.ucdavis.fiehnlab.mona.backend.curation.processor.{FinalizeCuration, RemoveComputedData}
import edu.ucdavis.fiehnlab.mona.backend.curation.reader.JSONFileSpectraReader
import edu.ucdavis.fiehnlab.mona.backend.curation.writer.RestRepositoryWriter
import org.springframework.amqp.core._
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.{ItemProcessor, ItemReader, ItemWriter}
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration, Import}

/**
  * defines a handful of different beans to simplify work with curation tasks
  * and assorted issues
  */
@Configuration
@ComponentScan(value = Array("edu.ucdavis.fiehnlab.mona.backend.curation.processor"))
@Import(Array(classOf[BusConfig]))
class CurationConfig extends LazyLogging {

  /**
    * in which queue will all curation tasks be stored. DO NOT RENAME!!!
    *
    * @return
    */
  @Bean(name = Array("spectra-curation-queue"))
  def queueName: String = "curation-queue"

  /**
    * the actual queue
    *
    * @return
    */
  @Bean(name = Array("spectra-curation-queue-instance"))
  def queue: Queue = {
    new Queue(queueName, false)
  }

  /**
    * which exchange will be used for the curation
    *
    * @return
    */
  @Bean
  def exchange: DirectExchange = {
    new DirectExchange("spectra-curation")
  }

  /**
    * just binding the different queues together
    *
    * @param queue
    * @param exchange
    * @return
    */
  @Bean
  def binding(queue: Queue, exchange: DirectExchange): Binding = {
    BindingBuilder.bind(queue).to(exchange).`with`(queueName)
  }


  @Bean
  def classifierProcessor = new ClassyfireProcessor

  /**
    * This defines the spectra curation workflow processor bean
    * and configures it for our use
    *
    * @return
    */
  @Bean
  def curationWorkflow(classifierProcessor: ClassyfireProcessor, calculateCompoundProperties: CalculateCompoundProperties): ItemProcessor[Spectrum, Spectrum] = {
    val flow: Workflow[Spectrum] = WorkflowBuilder
      .create[Spectrum]
      .enableAnnotationLinking(false)
      .forceLinear()
      .add(
        Array(
          new RemoveComputedData,

          // Compound curation
          calculateCompoundProperties,
          classifierProcessor,

          // Spectrum-level curation
          new NormalizeSpectrum,
          new CalculateSplash,
          new SpectrumIonCountScoringRule,

          // Metadata curation
          new SpectralEntropy,
          new NormalizeMetaDataNames,
          new IdentifyChromatography,
          new NormalizeIonizationModeValue,
          new NormalizeMSLevelValue,
          new NormalizePrecursorValues,
          new IdentifyMetaDataFields,

          // Validation
          new AdductPrediction,
          new CalculateMassAccuracy,
          new MassAccuracyValidation,

          // Calculate Adducts after other curations complete
          new CalculateAllAdducts,

          // Add validation metadata
          new FinalizeCuration
        )
      ).build()

    /**
      * ugly wrapper, but have no better alternative right now
      */
    new ItemProcessor[Spectrum, Spectrum] {
      override def process(item: Spectrum): Spectrum = flow.process(item).head
    }
  }

  /**
    * should be utilized in jobs to persist entities at the mona backend
    *
    * @return
    */
  @Bean
  @StepScope
  def restRepositoryWriter(@Value("#{jobParameters[loginToken]}") loginToken: String): ItemWriter[Spectrum] = {
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
  def jsonFileReader(@Value("#{jobParameters[pathToFile]}") file: String): ItemReader[Spectrum] = {
    if (file == null) {
      throw new FileNotFoundException("you need to provide a file name, but instead the parameter was null!")
    }

    val reader = new JSONFileSpectraReader()

    if (new File(file).exists()) {
      logger.debug("a file was provided")
      reader.stream = new BufferedInputStream(new FileInputStream(file))
    } else {
      logger.warn(s"provided file $file did not exist, trying to load from classpath")
      reader.stream = getClass.getResourceAsStream(file)
    }

    reader
  }
}
