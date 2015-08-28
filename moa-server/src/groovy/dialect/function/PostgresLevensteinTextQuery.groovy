package dialect.function

import org.hibernate.QueryException
import org.hibernate.dialect.function.SQLFunction
import org.hibernate.engine.spi.Mapping
import org.hibernate.engine.spi.SessionFactoryImplementor
import org.hibernate.type.DoubleType
import org.hibernate.type.Type

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 8/28/15
 * Time: 10:36 AM
 */
class PostgresLevensteinTextQuery  implements SQLFunction {
    @Override
    boolean hasArguments() {
        return true
    }

    @Override
    boolean hasParenthesesIfNoArguments() {
        return false
    }

    @Override
    Type getReturnType(Type type, Mapping mapping) throws QueryException {
        return new DoubleType();
    }

    @Override
    String render(Type type, List args, SessionFactoryImplementor sessionFactoryImplementor) throws QueryException {
        if (args != null && args.size() != 2) {
            throw new IllegalArgumentException(
                    "The function must be passed 2 arguments");
        }

        if (args.size() == 2) {
            return "levenshtein(${args.get(0)},${args.get(1)})";
        }
        return null;
    }
}
