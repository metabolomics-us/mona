package dialect

import dialect.function.PostgresLevensteinTextQuery
import dialect.function.PostgresSpectraMatch
import dialect.function.PostgresTextQuery
import org.hibernate.dialect.PostgreSQL9Dialect

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 8/11/15
 * Time: 1:27 PM
 */
class CustomPostgresDialect extends PostgreSQL9Dialect {

    CustomPostgresDialect() {
        registerFunction("textmatch", new PostgresTextQuery())
        registerFunction("spectramatch", new PostgresSpectraMatch())
        registerFunction("levenstein", new PostgresLevensteinTextQuery());
    }
}
