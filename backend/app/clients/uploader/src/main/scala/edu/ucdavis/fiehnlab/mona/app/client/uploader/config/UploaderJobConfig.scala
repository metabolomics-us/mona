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
@Import(Array(classOf[RestClientConfig], classOf[WorkflowConfiguration], classOf[CurrationConfig]))
@ImportResource(Array("classpath:uploadJob.xml"))
@ComponentScan(Array("edu.ucdavis.fiehnlab.mona.backend.curation"))
class UploaderJobConfig extends LazyLogging
