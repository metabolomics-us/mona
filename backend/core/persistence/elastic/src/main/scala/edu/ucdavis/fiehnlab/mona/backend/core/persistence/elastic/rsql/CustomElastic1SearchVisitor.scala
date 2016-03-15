package edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.rsql

import com.github.rutledgepaulv.qbuilders.nodes.ComparisonNode
import com.typesafe.scalalogging.LazyLogging
import org.elasticsearch.index.query.{FilterBuilder, QueryBuilder}

/**
  * Created by wohlg on 3/13/2016.
  */
class CustomElastic1SearchVisitor extends ElasticsearchVisitor with LazyLogging {

  /**
    * maps the field name from value to value_*
    * @param field
    * @param node
    * @param context
    *     */
  override protected def modifyFieldName(field: String, node: ComparisonNode, context: Context): String = field match {
    case "value" =>
      single(node.getValues) match {
        case x: Number =>
          logger.debug(s"building number  query for ${x}")
          "value_number"
        case x: java.lang.Boolean =>
          logger.debug(s"building boolean query for ${x}")
          "value_boolean"
        case _ =>
          logger.debug(s"building text query for ${node.getValues}")
          "value_text"
      }

    case _ => super.modifyFieldName(field,node,context)
  }
}
