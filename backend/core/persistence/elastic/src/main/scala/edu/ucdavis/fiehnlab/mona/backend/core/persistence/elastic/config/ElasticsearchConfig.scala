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

  /**
    * this defines our custom wired elastic search template
    *
    * @return
    */
  @Bean
  def elasticsearchTemplate(elasticClient: Client): ElasticsearchTemplate = {
    new CustomElasticsearchTemplate(elasticClient, new EntityMapper with LazyLogging {
      val mapper: ObjectMapper = MonaMapper.create
      val module: SimpleModule = new SimpleModule()

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
