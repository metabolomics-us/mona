package edu.ucdavis.fiehnlab.mona.app.node.config

/**
  * Created by wohlgemuth on 3/21/16.
  */
@Configuration
@Import(Array(classOf[ElasticsearchConfig], classOf[MongoConfig], classOf[RestServerConfig]))
class DatabaseConnectivity extends LazyLogging {


  @Primary
  @Bean
  def elasticClient: Client = {
    logger.info("creating embedded client for elastic")
    EmbeddedNodeBuilder.createClient(deleteOnExit = true, httpServer = true)
  }

}
