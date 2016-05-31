package rsql

import com.github.rutledgepaulv.qbuilders.nodes.ComparisonNode
import com.github.rutledgepaulv.qbuilders.operators.ComparisonOperator
import com.github.rutledgepaulv.qbuilders.visitors.MongoVisitor
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.rqe.regex.RegexStringFieldImpl
import org.springframework.data.mongodb.core.query.Criteria

/**
  * Created by sajjan on 5/31/16.
  */
class CustomMongoVisitor extends MongoVisitor with LazyLogging {
  override protected def visit(node: ComparisonNode): Criteria = {
    val operator: ComparisonOperator = node.getOperator

    // Handle custom regex query, or else pass the node to the default visitor
    if (operator.equals(RegexStringFieldImpl.REGEX)) {
      Criteria.where(node.getField.asKey).regex(node.getValues.iterator().next().toString)
    } else {
      super.visit(node)
    }
  }
}
