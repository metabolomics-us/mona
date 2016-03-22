package edu.ucdavis.fiehnlab.mona.app.client.uploader.config


import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.config.RestClientConfig
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.config.WorkflowConfiguration
import edu.ucdavis.fiehnlab.mona.backend.curation.config.CurrationConfig
import org.springframework.batch.core.configuration.annotation.{EnableBatchProcessing, JobBuilderFactory, StepBuilderFactory}
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.{Job, JobExecution, JobExecutionListener, Step}
import org.springframework.batch.item.{ItemProcessor, ItemReader, ItemWriter}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation._

/**
  * Created by wohlgemuth on 3/21/16.
  */
@Configuration
@EnableBatchProcessing
@Import(Array(classOf[RestClientConfig], classOf[WorkflowConfiguration],classOf[CurrationConfig]))
@ImportResource(Array("classpath:uploadJob.xml"))
@ComponentScan(Array("edu.ucdavis.fiehnlab.mona.backend.curation"))
class UploaderJobConfig extends LazyLogging {

  @Autowired
  val jobBuilderFactory: JobBuilderFactory = null

  @Autowired
  val stepBuilderFactory: StepBuilderFactory = null

  @Autowired
  val currationWorkflow:ItemProcessor[Spectrum,Spectrum] = null

  @Autowired
  val restRepositoryWriter: ItemWriter[Spectrum] = null

  @Autowired
  val jsonFileReader:ItemReader[Spectrum] = null


  @Bean
  def listener: JobExecutionListener = {
    new JobExecutionListener {

      override def beforeJob(jobExecution: JobExecution): Unit = logger.info("starting job...")

      override def afterJob(jobExecution: JobExecution): Unit = logger.info("finished job")
    }
  }

  @Bean
  def uploadSpectraStep: Step = {
    stepBuilderFactory.get("uploadSpectraStep").chunk(10).reader(jsonFileReader).writer(restRepositoryWriter).build()
  }

  @Bean
  def uploadSpectraJob: Job = {
    jobBuilderFactory.get("uploadSpectraJob").incrementer(new RunIdIncrementer).listener(listener).flow(uploadSpectraStep).end().build()

  }

}
