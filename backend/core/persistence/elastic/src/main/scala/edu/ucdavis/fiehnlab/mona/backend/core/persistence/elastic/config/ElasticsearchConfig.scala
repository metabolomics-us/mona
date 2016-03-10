package edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.config.DomainConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.ISpectrumElasticRepositoryCustom
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
@Import(Array(classOf[DomainConfig]))
class ElasticsearchConfig extends LazyLogging {

  // @Value("${mona.persistence.elastic.port}")
  val port: Int = 9300
  // @Value("${mona.persistence.elastic.host}")
  val hostname: String = "127.0.0.1"

  @Autowired
  val objectMapper:ObjectMapper = null

  @Bean
  def elasticsearchTemplate: ElasticsearchOperations = {

    //    val template= new ElasticsearchTemplate(new NodeBuilder().local(true).node().client());
    val template = new ElasticsearchTemplate(client,new EntityMapperImpl(objectMapper))
    template
  }

  @Bean
  def client: Client = {
    logger.info(s"connecting to ${hostname}:${port}")
    val client = new TransportClient()
    val address = new InetSocketTransportAddress(hostname, port)
    client.addTransportAddress(address)

    client
  }


  /**
    * takes care of all the seriz
    * @param mapper
    */
  class EntityMapperImpl(val mapper: ObjectMapper) extends EntityMapper {

    override def mapToString(`object`: scala.Any): String = mapper.writeValueAsString(`object`)

    override def mapToObject[T](source: String, clazz: Class[T]): T = mapper.readValue(source, clazz)
  }

}
