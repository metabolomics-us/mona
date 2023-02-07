package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.filter;

import com.turkraft.springfilter.boot.FilterSpecification;
import com.turkraft.springfilter.parser.Filter;
import com.turkraft.springfilter.parser.generator.expression.ExpressionGenerator;

import javax.persistence.criteria.*;
import java.util.HashMap;
import java.util.Map;

public class FilterSpecificationDistinct<T> extends FilterSpecification<T> {

    public FilterSpecificationDistinct(String input) {
        super(input);
    }

    public FilterSpecificationDistinct(Filter filter) {
        super(filter);
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        query.distinct(true);
        Predicate predicate = null;
        Map<String, Join<?, ?>> j = this.getJoins() != null ? this.getJoins() : new HashMap();
        if (this.getInput() != null) {
            predicate = !this.getInput().trim().isEmpty() ? (Predicate)ExpressionGenerator.run(Filter.from(this.getInput()), root, query, criteriaBuilder, (Map)j, this.getPayload()) : null;
        } else {
            predicate = (Predicate)ExpressionGenerator.run(this.getFilter(), root, query, criteriaBuilder, (Map)j, this.getPayload());
        }

        return predicate;
    }
}
