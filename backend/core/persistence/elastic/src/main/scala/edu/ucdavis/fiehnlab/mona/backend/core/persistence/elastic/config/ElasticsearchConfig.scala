package edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.config

import com.fasterxml.jackson.databind.module.SimpleModule
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.MetaData
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.MonaMapper
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.mapper.{ElasticMedaDataDeserializer, ElasticMetaDataSerializer, MappingUpdater}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.repository.ISpectrumElasticRepositoryCustom
import org.elasticsearch.client.Client
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.springframework.context.annotation._
import org.springframework.data.elasticsearch.core.{ElasticsearchTemplate, EntityMapper}
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
    new ElasticsearchTemplate(elasticClient, new EntityMapper with LazyLogging {

      val mapper = MonaMapper.create

      val module = new SimpleModule()

      module.addSerializer(classOf[MetaData], new ElasticMetaDataSerializer)
      module.addDeserializer(classOf[MetaData], new ElasticMedaDataDeserializer)


      mapper.registerModule(module)

      logger.debug("created new entity mapper for elastic specific operations")

      override def mapToString(`object`: scala.Any): String = mapper.writeValueAsString(`object`)

      override def mapToObject[T](source: String, clazz: Class[T]): T = mapper.readValue(source, clazz)
    }
    )
  }

  /**
    * this defines the elastic client and where we want to connect from
    *
    * @return
    */
  /*
  @Bean
  def elasticClient: Client = {
    logger.info(s"connecting to ${hostname}:${port}")
    val client = new TransportClient()
    val address = new InetSocketTransportAddress(hostname, port)
    client.addTransportAddress(address)

    client
  }

*/
  @Bean
  def mappingUpdater: MappingUpdater = new MappingUpdater

}
