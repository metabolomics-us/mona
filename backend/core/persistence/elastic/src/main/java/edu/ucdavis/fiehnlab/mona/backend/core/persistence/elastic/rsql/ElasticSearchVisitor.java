package edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.rsql;

import com.github.rutledgepaulv.qbuilders.nodes.AndNode;
import com.github.rutledgepaulv.qbuilders.nodes.ComparisonNode;
import com.github.rutledgepaulv.qbuilders.nodes.OrNode;
import com.github.rutledgepaulv.qbuilders.operators.ComparisonOperator;
import com.github.rutledgepaulv.qbuilders.visitors.ContextualNodeVisitor;
import edu.ucdavis.fiehnlab.rqe.like.LikeStringFieldImpl;
import edu.ucdavis.fiehnlab.rqe.regex.RegexStringFieldImpl;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.RegexpQueryBuilder;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.elasticsearch.index.query.QueryBuilders.*;

public class ElasticSearchVisitor extends ContextualNodeVisitor<QueryBuilder, Context> {

    protected static final Function<Object, Object> IDENTITY = object -> object;
    protected final Function<Object, Object> normalizer;

    public ElasticSearchVisitor() {
        this(IDENTITY);
    }

    public ElasticSearchVisitor(Function<Object, Object> normalizer) {
        this.normalizer = normalizer;
    }

    @Override
    protected QueryBuilder visit(AndNode node, Context context) {
        BoolQueryBuilder parent = boolQuery();
        node.getChildren().stream().map(child -> visitAny(child, context)).forEach(parent::must);
        return parent;
    }

    @Override
    protected QueryBuilder visit(OrNode node, Context context) {
        BoolQueryBuilder parent = boolQuery();
        node.getChildren().stream().map(child -> visitAny(child, context)).forEach(parent::should);
        return parent;
    }



    /**
     * modifies the field, in case it should be overwritten for specific types
     * @param field
     * @param node
     * @param context
     * @return
     */
    protected String modifyFieldName(String field, ComparisonNode node, Context context) {
        return field;
    }

    @Override
    protected QueryBuilder visit(ComparisonNode node, Context context) {
        ComparisonOperator operator = node.getOperator();

        Collection<?> values = node.getValues().stream().map(normalizer).collect(Collectors.toList());

        String field = modifyFieldName(node.getField().asKey(), node, context);

        if(context.getParent() != null) {
            field = context.buildNestedPath() + "." + field;
        }

        if (ComparisonOperator.EQ.equals(operator)) {
            return termQuery(field, single(values));
        } else if (ComparisonOperator.NE.equals(operator)) {
            return boolQuery().mustNot(termQuery(field, single(values)));
        }

        /*
        else if (ComparisonOperator.EX.equals(operator)) {
            if (single(values).equals(true)) {
                return existsQuery(field);
            } else {
                return boolQuery().mustNot(existsQuery(field));
            }
        }
        */

        else if (ComparisonOperator.GT.equals(operator)) {
            return rangeQuery(field).gt(single(values));
        } else if (ComparisonOperator.LT.equals(operator)) {
            return rangeQuery(field).lt(single(values));
        } else if (ComparisonOperator.GTE.equals(operator)) {
            return rangeQuery(field).gte(single(values));
        } else if (ComparisonOperator.LTE.equals(operator)) {
            return rangeQuery(field).lte(single(values));
        } else if (ComparisonOperator.IN.equals(operator)) {
            return termsQuery(field, values);
        } else if (ComparisonOperator.NIN.equals(operator)) {
            return boolQuery().mustNot(termsQuery(field, values));
        } else if (ComparisonOperator.SUB_CONDITION_ANY.equals(operator)) {
            // create a new context to pass to the children so we don't modify the one
            // that may get reused from "above"
            return nestedQuery(field, condition(node, context.createChieldContent(node.getField().asKey())));
        } else if(RegexStringFieldImpl.REGEX.equals(node.getOperator())){
            return new RegexpQueryBuilder(field, single(values).toString());
        } else if (LikeStringFieldImpl.LIKE.equals(node.getOperator())) {
            return boolQuery()
                    .should(queryStringQuery(single(values).toString()).defaultField(field).boost(10))
                    .should(queryStringQuery("*"+ single(values).toString() +"*").defaultField(field).rewrite("scoring_boolean"));
        } else {
            throw new UnsupportedOperationException("This visitor does not support the operator " + operator + ".");
        }
    }
}