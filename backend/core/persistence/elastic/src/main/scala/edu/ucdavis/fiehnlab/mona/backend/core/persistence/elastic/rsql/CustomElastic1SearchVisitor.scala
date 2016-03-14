package edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.rsql

import com.github.rutledgepaulv.qbuilders.builders.QBuilder
import com.github.rutledgepaulv.qbuilders.nodes.ComparisonNode
import com.github.rutledgepaulv.qbuilders.operators.ComparisonOperator
import com.github.rutledgepaulv.qbuilders.visitors.ElasticsearchVisitor
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import org.elasticsearch.index.query.{FilterBuilder, QueryBuilder}

/**
  * Created by wohlg on 3/13/2016.
  */
class CustomElastic1SearchVisitor extends ElasticsearchVisitor with LazyLogging{

  /**
    * checks for value fields and makes sure they are correctly formated for the queries
    * it would be nicer to utilize annotations for this, but its sadly not possible
    *
    * @param node
    * @return
    */
  override def visit(node: ComparisonNode): QueryBuilder = {

    //rename fields
    node.getField match {
      case "value" =>
        single(node.getValues) match {
          case x:Number =>
            logger.debug(s"building number  query for ${x}")
            node.setField("value_number")
          case x:java.lang.Boolean =>
            logger.debug(s"building boolean query for ${x}")
            node.setField("value_boolean")
          case _ =>
            logger.debug(s"building text query for ${node.getValues}")
            node.setField("value_text")
        }

      case _ =>
    }

    super.visit(node)
  }
}
