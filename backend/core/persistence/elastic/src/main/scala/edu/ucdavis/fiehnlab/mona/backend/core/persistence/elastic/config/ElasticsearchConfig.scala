package edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.config

import edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.ISpectrumElasticRepositoryCustom
import org.elasticsearch.node.NodeBuilder
import org.springframework.context.annotation.{Bean, Configuration, PropertySource}
import org.springframework.data.elasticsearch.core.{ElasticsearchTemplate, ElasticsearchOperations}
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories

/**
  * Created by wohlg_000 on 3/9/2016.
  */
@Configuration
@EnableElasticsearchRepositories(basePackageClasses = Array(
  classOf[ISpectrumElasticRepositoryCustom]
), excludeFilters = Array())
class ElasticsearchConfig {

  @Bean
  def elasticsearchTemplate: ElasticsearchOperations = {
    new ElasticsearchTemplate(new NodeBuilder().local(true).node().client());
  }
}
