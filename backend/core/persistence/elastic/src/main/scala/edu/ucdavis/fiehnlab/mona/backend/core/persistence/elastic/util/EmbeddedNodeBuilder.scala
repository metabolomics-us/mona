package edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.util

import java.io.File

import com.typesafe.scalalogging.LazyLogging
import org.elasticsearch.client.{Client, ElasticsearchClient}
import org.elasticsearch.common.logging.ESLoggerFactory
import org.elasticsearch.node.NodeBuilder

/**
  * Created by wohlgemuth on 3/21/16.
  */
object EmbeddedNodeBuilder extends LazyLogging{

  /**
    * configures an internal elastic search server
    * @param deleteOnExit
    * @param httpServer
    * @return
    */
  def createClient(deleteOnExit:Boolean = true, httpServer:Boolean = true):Client = {

    val fileElasticDirectory = new File(File.createTempFile("elastic-temp","temp").getParent,s"elastic-${System.currentTimeMillis()}")
    fileElasticDirectory.mkdirs()
    if(deleteOnExit) {
      fileElasticDirectory.deleteOnExit()
    }

    logger.info("creating new client")
    ESLoggerFactory.getRootLogger().setLevel("DEBUG");

    val nodeBuilder = new NodeBuilder()
    nodeBuilder.local(true)
    nodeBuilder.settings().put("http.enabled", httpServer)
    nodeBuilder.settings().put("index.search.slowlog.threshold.query.warn","0ms")
    nodeBuilder.settings().put("index.search.slowlog.threshold.fetch.debug","0ms")
    nodeBuilder.settings().put("index.indexing.slowlog.threshold.query.info","0ms")

    nodeBuilder.settings().put("path.data",new File(fileElasticDirectory,"data").getAbsolutePath)
    nodeBuilder.settings().put("path.logs",new File(fileElasticDirectory,"logs").getAbsolutePath)
    nodeBuilder.settings().put("path.work",new File(fileElasticDirectory,"work").getAbsolutePath)


    logger.info("created client")
    nodeBuilder.node().client()
  }
}
