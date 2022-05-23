package edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.MonaMapper
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{MetaData, Names, Submitter, Tags}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.mapper.{AnalyzedStringSerializer, ElasticMetaDataDeserializer, ElasticMetaDataSerializer, MappingUpdater}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.repository.ISpectrumElasticRepositoryCustom
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.rsql.CustomElasticsearchTemplate
import org.elasticsearch.client.Client
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation._
import org.springframework.data.elasticsearch.core.{ElasticsearchTemplate, EntityMapper}
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.{InetSocketTransportAddress, TransportAddress}
import org.elasticsearch.transport.client.PreBuiltTransportClient
import org.springframework.context.annotation.Bean

import java.net.InetAddress

/**
  * Created by wohlg_000 on 3/9/2016.
  */
@Configuration
@EnableElasticsearchRepositories(basePackageClasses = Array(
  classOf[ISpectrumElasticRepositoryCustom]
))
@ComponentScan(basePackageClasses = Array(classOf[ISpectrumElasticRepositoryCustom]))
class ElasticsearchConfig extends LazyLogging {
  @Bean
  def elasticsearchClient: Client = {
    val settings = Settings.builder.put("cluster.name", "elasticsearch").build
    val client = new PreBuiltTransportClient(settings)
    client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"), 9300))
    client
  }
  /**
    * this defines our custom wired elastic search template
    *
    * @return
    */
  @Bean(name = Array("elasticsearchOperations", "elasticsearchTemplate"))
  def elasticsearchTemplate(): ElasticsearchTemplate = {
    new CustomElasticsearchTemplate(elasticsearchClient, new EntityMapper with LazyLogging {
      val mapper: ObjectMapper = MonaMapper.create
      val module = new SimpleModule()

      module.addSerializer(classOf[MetaData], new ElasticMetaDataSerializer)
      module.addDeserializer(classOf[MetaData], new ElasticMetaDataDeserializer)
      module.addSerializer(classOf[Names], new AnalyzedStringSerializer[Names])
      module.addSerializer(classOf[Tags], new AnalyzedStringSerializer[Tags])
      module.addSerializer(classOf[Submitter], new AnalyzedStringSerializer[Submitter])

      mapper.registerModule(module)

      logger.debug("created new entity mapper for elastic specific operations")

      override def mapToString(`object`: scala.Any): String = mapper.writeValueAsString(`object`)

      override def mapToObject[T](source: String, clazz: Class[T]): T = mapper.readValue(source, clazz)
    })
  }

  @Bean
  def mappingUpdater: MappingUpdater = new MappingUpdater
}
