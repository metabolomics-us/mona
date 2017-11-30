package edu.ucdavis.fiehnlab.mona.app.client.uploader.config


import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.config.RestClientConfig
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.config.WorkflowConfiguration
import edu.ucdavis.fiehnlab.mona.backend.curation.config.CurationConfig
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.context.annotation._

/**
  * Created by wohlgemuth on 3/21/16.
  */
@Configuration
@EnableBatchProcessing
@Import(Array(classOf[RestClientConfig], classOf[WorkflowConfiguration], classOf[CurationConfig]))
@ImportResource(Array("classpath:uploadJob.xml"))
@ComponentScan(Array("edu.ucdavis.fiehnlab.mona.backend.curation"))
class UploaderJobConfig extends LazyLogging {


}
