package edu.ucdavis.fiehnlab.mona.app.client.uploader.config


import java.io.{FileInputStream, BufferedInputStream, File}

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.config.RestClientConfig
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.config.WorkflowConfiguration
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.{LinearWorkflow, Workflow}
import edu.ucdavis.fiehnlab.mona.backend.curation.reader.JSONFileSpectraReader
import edu.ucdavis.fiehnlab.mona.backend.curation.writer.RestRepositoryWriter
import org.springframework.batch.core.{JobExecution, JobExecutionListener, Step, Job}
import org.springframework.batch.core.configuration.annotation.{StepScope, StepBuilderFactory, JobBuilderFactory, EnableBatchProcessing}
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.item.{ItemReader, ItemProcessor, ItemWriter}
import org.springframework.beans.factory.annotation.{Value, Autowired}
import org.springframework.context.annotation.{ComponentScan, Import, Bean, Configuration}

/**
  * Created by wohlgemuth on 3/21/16.
  */
@Configuration
@EnableBatchProcessing
@Import(Array(classOf[RestClientConfig], classOf[WorkflowConfiguration]))
@ComponentScan(Array("edu.ucdavis.fiehnlab.mona.backend.curation"))
class UploaderJobConfig extends LazyLogging {

  @Autowired
  val jobBuilderFactory: JobBuilderFactory = null

  @Autowired
  val stepBuilderFactory: StepBuilderFactory = null

  /**
    * writer to utilize
    *
    * @return
    */
  @Bean
  def restRepositoryWriter: ItemWriter[Spectrum] = {
    new RestRepositoryWriter()
  }

  /**
    * reader to utilize
    *
    * @return
    */
  @Bean
  @StepScope
  def jsonFileReader(@Value("#{jobParameters[pathToFile]}")
                     file: String): ItemReader[Spectrum] = {
    val reader = new JSONFileSpectraReader()

    if(new File(file).exists()){
      logger.debug("a file was provided")
      reader.stream = new BufferedInputStream(new FileInputStream(file))
    }
    else{
      logger.warn("provided file did not exist, trying to load from classpath")
      reader.stream = getClass.getResourceAsStream(file)
    }

    reader
  }

  @Bean
  def listener: JobExecutionListener = {
    new JobExecutionListener {

      override def beforeJob(jobExecution: JobExecution): Unit = logger.info("starting job...")

      override def afterJob(jobExecution: JobExecution): Unit = logger.info("finished job")
    }
  }

  /**
    * generate a workflow of the name curation
    *
    * @return
    */
  @Bean
  def process: ItemProcessor[Spectrum, Spectrum] = {
    new LinearWorkflow[Spectrum](name = "spectra-curration")
  }

  @Bean
  def uploadSpectraStep: Step = {
    stepBuilderFactory.get("uploadSpectraStep").chunk(10).reader(jsonFileReader(null)).writer(restRepositoryWriter).build()
  }

  @Bean
  def uploadSpectraJob: Job = {
    jobBuilderFactory.get("uploadSpectraJob").incrementer(new RunIdIncrementer).listener(listener).flow(uploadSpectraStep).end().build()

  }

  /*
    @Bean
    def uploadAndCurrationSpectraStep: Step = {
      stepBuilderFactory.get("uploadAndCurrationSpectraStep").chunk(10).reader(jsonFileReader).processor(process).writer(restRepositoryWriter).build()
      null
    }

    @Bean
    def uploadAndCurrationSpectraJob: Job = {
      jobBuilderFactory.get("uploadAndCurrationSpectraJob").incrementer(new RunIdIncrementer).listener(listener).flow(uploadAndCurrationSpectraStep).end().build()
    }

    */
}
