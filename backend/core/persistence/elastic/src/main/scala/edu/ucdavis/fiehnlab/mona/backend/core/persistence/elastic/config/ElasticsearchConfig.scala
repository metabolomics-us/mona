package edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.config.DomainConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.mapper.{MappingUpdater, EntityMapperImpl}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.repository.ISpectrumElasticRepositoryCustom
import org.elasticsearch.client.Client
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.transport.{InetSocketTransportAddress, TransportAddress}
import org.elasticsearch.node.NodeBuilder
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.context.annotation._
import org.springframework.data.elasticsearch.core.{EntityMapper, ElasticsearchTemplate, ElasticsearchOperations}
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories

/**
  * Created by wohlg_000 on 3/9/2016.
  */
@Configuration
@EnableElasticsearchRepositories(basePackageClasses = Array(
  classOf[ISpectrumElasticRepositoryCustom]
))
@ComponentScan(basePackageClasses = Array(classOf[ISpectrumElasticRepositoryCustom]))
class ElasticsearchConfig extends LazyLogging {

  // @Value("${mona.persistence.elastic.port}")
  val port: Int = 9300
  // @Value("${mona.persistence.elastic.host}")
  val hostname: String = "127.0.0.1"

  /**
    * this defines our custom wired elastic search template
    *
    * @return
    */
  @Bean
  def elasticsearchTemplate(elasticClient: Client): ElasticsearchTemplate = {
    new ElasticsearchTemplate(elasticClient, new EntityMapperImpl())
  }

  /**
    * this defines the elastic client and where we want to connect from
    *
    * @return
    */
  @Bean
  def elasticClient: Client = {
    logger.info(s"connecting to ${hostname}:${port}")
    val client = new TransportClient()
    val address = new InetSocketTransportAddress(hostname, port)
    client.addTransportAddress(address)

    client
  }

  @Bean
  def mappingUpdater: MappingUpdater = new MappingUpdater

}
