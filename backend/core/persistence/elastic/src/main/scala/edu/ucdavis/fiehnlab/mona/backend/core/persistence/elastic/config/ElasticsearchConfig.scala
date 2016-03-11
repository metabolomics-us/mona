package edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.config.DomainConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.ISpectrumElasticRepositoryCustom
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.mapper.{EntityMapperImpl}
import org.elasticsearch.client.Client
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.transport.{InetSocketTransportAddress, TransportAddress}
import org.elasticsearch.node.NodeBuilder
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.context.annotation.{Import, Bean, Configuration, PropertySource}
import org.springframework.data.elasticsearch.core.{EntityMapper, ElasticsearchTemplate, ElasticsearchOperations}
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories

/**
  * Created by wohlg_000 on 3/9/2016.
  */
@Configuration
@EnableElasticsearchRepositories(basePackageClasses = Array(
  classOf[ISpectrumElasticRepositoryCustom]
))
class ElasticsearchConfig extends LazyLogging {

  // @Value("${mona.persistence.elastic.port}")
  val port: Int = 9300
  // @Value("${mona.persistence.elastic.host}")
  val hostname: String = "127.0.0.1"

  /**
    * this defines our custom wired elastic search template
    * @return
    */
  @Bean
  def elasticsearchTemplate: ElasticsearchOperations = {

    //val template= new ElasticsearchTemplate(new NodeBuilder().local(true).node().client(),new EntityMapperImpl())
    val template = new ElasticsearchTemplate(client,new EntityMapperImpl())
    template
  }

  /**
    * this defines the elastic client and where we want to connect from
    * @return
    */
  @Bean
  def client: Client = {
    logger.info(s"connecting to ${hostname}:${port}")
    val client = new TransportClient()
    val address = new InetSocketTransportAddress(hostname, port)
    client.addTransportAddress(address)

    client
  }

}
