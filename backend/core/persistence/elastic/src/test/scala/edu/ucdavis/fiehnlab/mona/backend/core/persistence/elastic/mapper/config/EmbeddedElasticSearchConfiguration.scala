package edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.mapper.config

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.config.ElasticsearchConfig
import org.elasticsearch.action.delete.DeleteRequest
import org.elasticsearch.client.Client
import org.elasticsearch.common.logging.ESLoggerFactory;

import edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.ISpectrumElasticRepositoryCustom
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.mapper.EntityMapperImpl
import org.elasticsearch.node.NodeBuilder
import org.springframework.context.annotation.{Primary, Import, Bean, Configuration}
import org.springframework.data.elasticsearch.core.{ElasticsearchTemplate, ElasticsearchOperations}
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

  /**
    * defines an embeded server, which has web access
 *
    * @return
    */
  @Primary
  @Bean
  def elasticsearchTemplate(client:Client) : ElasticsearchOperations = {


    logger.info("force deletion of index")


    logger.info("creating new template")
    val elasticsearchTemplate = new ElasticsearchTemplate(
      client, new EntityMapperImpl())

    //force recreationg of indexes
    logger.info("deleting index")
    elasticsearchTemplate.deleteIndex(classOf[Spectrum])
    elasticsearchTemplate.createIndex(classOf[Spectrum])
    elasticsearchTemplate.refresh(classOf[Spectrum], true)

    logger.info("template is done")
    elasticsearchTemplate
  }

  @Primary
  @Bean
  def elasticClient:Client = {

    ESLoggerFactory.getRootLogger().setLevel("DEBUG");

    val nodeBuilder = new NodeBuilder()
    nodeBuilder.local(true)
    nodeBuilder.settings().put("http.enabled", true)
    nodeBuilder.settings().put("index.search.slowlog.threshold.query.warn","0ms")

    logger.info("creating client")
    nodeBuilder.node().client()
  }
}
