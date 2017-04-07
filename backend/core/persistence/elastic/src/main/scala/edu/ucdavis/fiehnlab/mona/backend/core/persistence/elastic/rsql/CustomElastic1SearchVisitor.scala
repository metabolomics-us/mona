package edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.rsql

import com.github.rutledgepaulv.qbuilders.nodes.ComparisonNode
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.rqe.like.LikeStringFieldImpl

/**
  * Created by wohlg on 3/13/2016.
  */
class CustomElastic1SearchVisitor extends ElasticSearchVisitor with LazyLogging {

  /**
    * maps the field name from value to value_*
    *
    * @param field
    * @param node
    * @param context
    *     */
  override protected def modifyFieldName(field: String, node: ComparisonNode, context: Context): String = field match {
    case "value" | "metaData.value" | "annotations.value" | "compound.metaData.value" | "compound.classification.value" =>
      single(node.getValues) match {
        case x: Number =>
          "value_number"

        case x: java.lang.Boolean =>
          "value_boolean"

        case _ =>
          // For like searches, use the analyzed field
          if (node.getOperator == LikeStringFieldImpl.LIKE)
            "value_text_analyzed"
          else
            "value_text"
      }

    case "name" | "names.name" | "metaData.name" | "annotations.name" | "compound.metaData.name" | "compound.classification.name" | "text" | "tags.text" =>
      // For like searches, use the analyzed field
      if (node.getOperator == LikeStringFieldImpl.LIKE)
        s"${field}_analyzed"
      else
        super.modifyFieldName(field, node, context)

    case _ =>
      super.modifyFieldName(field, node, context)
  }
}
