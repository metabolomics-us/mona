package edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.mapper.config

import java.io.File

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.config.ElasticsearchConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.repository.ISpectrumElasticRepositoryCustom
import org.elasticsearch.client.Client
import org.elasticsearch.common.logging.ESLoggerFactory;
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

  @Primary
  @Bean
  def elasticClient:Client = {

    val fileElasticDirectory = new File(File.createTempFile("elastic-temp","temp").getParent,s"elastic-${System.currentTimeMillis()}")
    fileElasticDirectory.mkdirs()
    fileElasticDirectory.deleteOnExit()

    logger.info("creating new client")
    ESLoggerFactory.getRootLogger().setLevel("DEBUG");

    val nodeBuilder = new NodeBuilder()
    nodeBuilder.local(true)
    nodeBuilder.settings().put("http.enabled", true)
    nodeBuilder.settings().put("index.search.slowlog.threshold.query.warn","0ms")
    nodeBuilder.settings().put("path.data",new File(fileElasticDirectory,"data").getAbsolutePath)
    nodeBuilder.settings().put("path.logs",new File(fileElasticDirectory,"logs").getAbsolutePath)
    nodeBuilder.settings().put("path.work",new File(fileElasticDirectory,"work").getAbsolutePath)


    logger.info("created client")
    nodeBuilder.node().client()
  }
}
