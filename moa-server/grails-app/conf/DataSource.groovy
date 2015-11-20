/**
 * shared settings
 */

def credentials = [
        port:System.getenv("EXTERNAL_POSTGRES_SERVICE_PORT"),
        hostname:System.getenv("EXTERNAL_POSTGRES_SERVICE_HOST"),
        username:System.getenv("POSTGRESQL_USER"),
        password:System.getenv("POSTGRESQL_PASSWORD"),
        database:System.getenv("POSTGRESQL_DATABASE")
]

dataSource {
    pooled = true
    driverClassName = "org.postgresql.Driver"
    username = "compound"
    password = "asdf"
    dialect = "dialect.CustomPostgresDialect"
}

hibernate {
    cache.use_second_level_cache = true
    cache.use_query_cache = false
//    cache.region.factory_class = 'net.sf.ehcache.hibernate.EhCacheRegionFactory' // Hibernate 3
    cache.region.factory_class = 'org.hibernate.cache.ehcache.EhCacheRegionFactory' // Hibernate 4
    format_sql = true
    use_sql_comments = true
}

environments {

    development {


        dataSource {
            dbCreate = "update"
            url = "jdbc:postgresql://venus.fiehnlab.ucdavis.edu:5432/monadevel"


            properties {
                jmxEnabled = true
                initialSize = 50
                maxActive = 70
                minIdle = 10
                maxIdle = 25
                maxWait = 10000
                maxAge = 10 * 60000
                timeBetweenEvictionRunsMillis = 5000
                minEvictableIdleTimeMillis = 60000
                validationQuery = "SELECT 1"
                validationQueryTimeout = 3
                validationInterval = 15000
                testOnBorrow = true
                testWhileIdle = true
                testOnReturn = false
                jdbcInterceptors = "ConnectionState"
                defaultTransactionIsolation = java.sql.Connection.TRANSACTION_READ_COMMITTED
            }

            logSql = false

        }
    }

    test {
        dataSource {
            dbCreate = "update"// "create-drop"
            url = "jdbc:postgresql://venus.fiehnlab.ucdavis.edu:5432/moa-devel"
            driverClassName = "org.postgresql.Driver"

            pooled = true

            cache.use_second_level_cache = false
            cache.use_query_cache = false
            logSql = false
        }
    }

    /**
     * mona production database
     */
    production {


        dataSource {
	     pooled = true
            dbCreate = "update"
//            url = "jdbc:postgresql://${credentials.hostname}:${credentials.port}/${credentials.database}"
//            username = "${credentials.username}"
//            password = "${credentials.password}"
            url = "jdbc:postgresql://128.120.143.126:5432/monaproduction"
            username = "compound"
            password = "asdf"
            logSql = false


            properties {
                jmxEnabled = true
                initialSize = 5
                maxActive = 100
                minIdle = 5
                maxIdle = 25
                maxWait = 10000
                maxAge = 10 * 60000
                timeBetweenEvictionRunsMillis = 5000
                minEvictableIdleTimeMillis = 60000
                validationQuery = "SELECT 1"
                validationQueryTimeout = 300
                validationInterval = 15000
                testOnBorrow = true
                testWhileIdle = true
                testOnReturn = false
                jdbcInterceptors = "ConnectionState;StatementCache(max=200)"
                defaultTransactionIsolation = java.sql.Connection.TRANSACTION_READ_COMMITTED
            }

        }
    }

}
