package edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.mapper.config

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import org.elasticsearch.action.delete.DeleteRequest
import org.elasticsearch.common.logging.ESLoggerFactory;

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
class EmbeddedElasticSearchConfiguration extends LazyLogging{

  /**
    * defines an embeded server, which has web access
 *
    * @return
    */
  @Bean
  def elasticsearchTemplate: ElasticsearchOperations = {

    ESLoggerFactory.getRootLogger().setLevel("DEBUG");

    val nodeBuilder = new NodeBuilder()
    nodeBuilder.local(true)
    nodeBuilder.settings().put("http.enabled", true)

    logger.info("creating client")
    val client = nodeBuilder.node().client()

    logger.info("force deletion of index")


    logger.info("creating new template")
    val elasticsearchTemplate = new ElasticsearchTemplate(
      client, new EntityMapperImpl())

    elasticsearchTemplate.deleteIndex(classOf[Spectrum])
    elasticsearchTemplate.createIndex(classOf[Spectrum])
    elasticsearchTemplate.refresh(classOf[Spectrum], true)

    elasticsearchTemplate
  }
}
