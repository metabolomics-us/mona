package edu.ucdavis.fiehnlab.mona.app.node.config

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.config.ElasticsearchConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.util.EmbeddedNodeBuilder
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config.MongoConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.config.RestServerConfig
import org.elasticsearch.client.Client
import org.springframework.context.annotation.{Bean, Configuration, Import, Primary}

/**
  * Created by wohlgemuth on 3/21/16.
  */
@Configuration
@Import(Array(classOf[ElasticsearchConfig], classOf[MongoConfig],classOf[RestServerConfig]))
class DatabaseConnectivity extends LazyLogging{


  @Primary
  @Bean
  def elasticClient: Client = {
    logger.info("creating embedded client for elastic")
    EmbeddedNodeBuilder.createClient(deleteOnExit = true, httpServer = true)
  }

}
