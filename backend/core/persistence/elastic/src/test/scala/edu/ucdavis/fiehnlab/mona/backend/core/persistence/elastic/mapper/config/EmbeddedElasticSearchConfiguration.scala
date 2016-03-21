package edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.mapper.config

import java.io.File

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.config.ElasticsearchConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.repository.ISpectrumElasticRepositoryCustom
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.util.EmbeddedNodeBuilder
import org.elasticsearch.client.Client
import org.springframework.context.annotation.{Primary, Import, Bean, Configuration}
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories

/**
  * Created by wohlg on 3/11/2016.
  */
@Configuration
@EnableElasticsearchRepositories(basePackageClasses = Array(
  classOf[ISpectrumElasticRepositoryCustom]
))
@Import(Array(classOf[ElasticsearchConfig]))
class EmbeddedElasticSearchConfiguration extends LazyLogging{

  @Primary
  @Bean
  def elasticClient:Client = {
    EmbeddedNodeBuilder.createClient(deleteOnExit = true,httpServer = true)
  }
}
