package rsql

import java.util.regex.Pattern

import com.github.rutledgepaulv.qbuilders.nodes.ComparisonNode
import com.github.rutledgepaulv.qbuilders.operators.ComparisonOperator
import com.github.rutledgepaulv.qbuilders.visitors.MongoVisitor
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.rqe.like.LikeStringFieldImpl
import edu.ucdavis.fiehnlab.rqe.regex.RegexStringFieldImpl
import org.springframework.data.mongodb.core.query.Criteria

/**
  * Created by sajjan on 5/31/16.
  */
class CustomMongoVisitor extends MongoVisitor with LazyLogging {
  override protected def visit(node: ComparisonNode): Criteria = {
    val operator: ComparisonOperator = node.getOperator

    // Handle custom regex and like queries, or else pass the node to the default visitor
    if (operator.equals(RegexStringFieldImpl.REGEX) || operator.equals(LikeStringFieldImpl.LIKE)) {
      val pattern: Pattern = Pattern.compile(node.getValues.iterator().next().toString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)
      Criteria.where(node.getField.asKey).regex(pattern)
    } else {
      super.visit(node)
    }
  }
}
