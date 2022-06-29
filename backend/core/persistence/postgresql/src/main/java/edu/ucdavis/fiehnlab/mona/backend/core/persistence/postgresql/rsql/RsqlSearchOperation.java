package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.rsql;

import cz.jirutka.rsql.parser.ast.ComparisonOperator;
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.rsql.RSQLOperatorsCustom;

import java.util.Set;

public enum RsqlSearchOperation {
    EQUAL(RSQLOperatorsCustom.EQUAL),
    NOT_EQUAL(RSQLOperatorsCustom.NOT_EQUAL),
    GREATER_THAN(RSQLOperatorsCustom.GREATER_THAN),
    GREATER_THAN_OR_EQUAL(RSQLOperatorsCustom.GREATER_THAN_OR_EQUAL),
    LESS_THAN(RSQLOperatorsCustom.LESS_THAN),
    LESS_THAN_OR_EQUAL(RSQLOperatorsCustom.LESS_THAN_OR_EQUAL),
    IN(RSQLOperatorsCustom.IN),
    NOT_IN(RSQLOperatorsCustom.NOT_IN),
    LIKE(RSQLOperatorsCustom.LIKE),
    NOT_LIKE(RSQLOperatorsCustom.NOT_LIKE);

    private ComparisonOperator operator;

    private RsqlSearchOperation(ComparisonOperator operator) {
        this.operator = operator;
    }

    public static RsqlSearchOperation getSimpleOperator(ComparisonOperator operator) {
        for (RsqlSearchOperation operation : values()) {
            if (operation.getOperator() == operator) {
                return operation;
            }
        }
        return null;
    }

    private ComparisonOperator getOperator() {
        return operator;
    }
}
