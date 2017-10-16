package edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.mapper

import javax.annotation.PostConstruct

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import org.elasticsearch.client.Client
import org.elasticsearch.common.xcontent.XContentBuilder
import org.elasticsearch.common.xcontent.XContentFactory._
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.DependsOn
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.stereotype.Component

/**
  * Created by wohlgemuth on 3/15/16.
  */
@Component
@DependsOn(Array("elasticsearchTemplate"))
class MappingUpdater extends LazyLogging {

  @Autowired
  val elasticClient: Client = null

  @Autowired
  val elasticsearchTemplate: ElasticsearchOperations = null

  /**
    * runs after bean creation
    */
  @PostConstruct
  def updateMappings(): Unit = {

    logger.debug("creating index")
    //elasticsearchTemplate.deleteIndex(classOf[Spectrum])
    elasticsearchTemplate.createIndex(classOf[Spectrum])

    logger.debug("refreshing index")
    elasticsearchTemplate.refresh(classOf[Spectrum], true)

    logger.debug("updating mapping")
    updateTextValueMapping()

    logger.debug("refreshing index")
    elasticsearchTemplate.refresh(classOf[Spectrum], true)
  }

  /**
    * generates a mapping for the text value field to ensure its not analyze
    */
  protected def updateTextValueMapping(): Unit = {

    val typeName = "spectrum"
    val indexName = "spectrum"

    logger.debug(s"object is mapped as $typeName")

    var build = jsonBuilder().prettyPrint()

    build = build.startObject()

    //builds the update for hte metadata
    build = build.startObject(typeName)
    build = build.startObject("properties")

    build = buildMetaData(build, "metaData")
    build = buildMetaData(build, "annotations")

    //build the compound properties
    build = build.startObject("compound").field("type", "nested")

    build = build.startObject("properties")
    build = buildMetaData(build, "metaData")
    build = buildMetaData(build, "classification")

    build = build.endObject()
    build = build.endObject()

    build = build.endObject()
    build = build.endObject()
    build = build.endObject()

    logger.debug(s"sending new mapping to server")

    elasticClient.admin().indices().preparePutMapping(indexName).setType(typeName).setSource(build).execute().actionGet()

    logger.debug("mapping should be updated")
  }

  /**
    * builds a metadata envelope
    *
    * @param builder
    * @return
    */
  protected def buildMetaData(builder: XContentBuilder, fieldName: String): XContentBuilder = {
    builder
      .startObject(fieldName)
      .field("type", "nested")
      .startObject("properties")
      .startObject("value_text_analyzed")
      .field("type", "string")
      .field("index", "analyzed")
      .endObject()
      .startObject("value_text")
      .field("type", "string")
      .field("index", "not_analyzed")
      .endObject()
      .startObject("value_number")
      .field("type", "double")
      .field("index", "not_analyzed")
      .endObject()
      .endObject()
      .endObject()
  }
}
