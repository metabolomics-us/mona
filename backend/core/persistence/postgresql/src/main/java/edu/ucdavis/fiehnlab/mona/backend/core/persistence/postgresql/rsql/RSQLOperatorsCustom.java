package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.rsql;

import cz.jirutka.rsql.parser.ast.ComparisonOperator;
import cz.jirutka.rsql.parser.ast.RSQLOperators;

import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;

public class RSQLOperatorsCustom extends RSQLOperators{

    public static final ComparisonOperator LIKE = new ComparisonOperator("=like=", true);

    public static final ComparisonOperator NOT_LIKE = new ComparisonOperator("=notlike=", true);

    public Set<ComparisonOperator> operators = RSQLOperators.defaultOperators();

    public static Set<ComparisonOperator> newDefaultOperators() {
        return new HashSet<>(asList(EQUAL, NOT_EQUAL, GREATER_THAN, GREATER_THAN_OR_EQUAL,
                LESS_THAN, LESS_THAN_OR_EQUAL, IN, NOT_IN, LIKE, NOT_LIKE));
    }
}
