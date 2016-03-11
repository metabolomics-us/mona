package edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.mapper.config

import edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.ISpectrumElasticRepositoryCustom
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.mapper.EntityMapperImpl
import org.elasticsearch.node.NodeBuilder
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.data.elasticsearch.core.{ElasticsearchTemplate, ElasticsearchOperations}
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories

/**
  * Created by wohlg on 3/11/2016.
  */
@Configuration
@EnableElasticsearchRepositories(basePackageClasses = Array(
  classOf[ISpectrumElasticRepositoryCustom]
))
class EmbeddedElasticSearchConfiguration {

  /**
    * defines an embeded server, which has web access
    * @return
    */
  @Bean
  def elasticsearchTemplate: ElasticsearchOperations = {

    val nodeBuilder = new NodeBuilder()
    nodeBuilder.local(true)
    nodeBuilder.settings().put("http.enabled", true)

    val client = nodeBuilder.node().client()
    val template = new ElasticsearchTemplate(
      client, new EntityMapperImpl())
    template
  }
}
