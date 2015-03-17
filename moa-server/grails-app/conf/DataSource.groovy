dataSource {
    pooled = true
    driverClassName = "org.h2.Driver"
    username = "sa"
    password = ""
}
hibernate {
    cache.use_second_level_cache = true
    cache.use_query_cache = true
    cache.region.factory_class = 'net.sf.ehcache.hibernate.EhCacheRegionFactory' // Hibernate 3
//    cache.region.factory_class = 'org.hibernate.cache.ehcache.EhCacheRegionFactory' // Hibernate 4
    format_sql = true
    use_sql_comments = true
}

environments {

    development {

        dataSource {
            dbCreate = "update"
            url = "jdbc:postgresql://venus.fiehnlab.ucdavis.edu:5432/moa-devel"
            driverClassName="org.postgresql.Driver"
            username="compound"
            password="asdf"
            pooled = true

            properties {

                jmxEnabled = true
                initialSize = 5

                //quartz threads + 10!
                maxActive = 50
                minIdle = 5
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
                jdbcInterceptors = "ConnectionState;StatementCache(max=200)"
                defaultTransactionIsolation = java.sql.Connection.TRANSACTION_READ_COMMITTED
                removeAbandoned = true
                logAbandoned = true
            }

        }

    }

    test {
        dataSource {
            dbCreate = "create-drop"
            url = "jdbc:postgresql://venus.fiehnlab.ucdavis.edu:5432/moa-test"
            driverClassName="org.postgresql.Driver"
            username="compound"
            password="asdf"
            pooled = true

            cache.use_second_level_cache = false
            cache.use_query_cache = false
            logSql = true
        }
    }

    /**
     * mona production database
     */
    production {
        dataSource {
            dbCreate = "update"
            url = "jdbc:postgresql://venus.fiehnlab.ucdavis.edu:5432/moa-prod"
            driverClassName="org.postgresql.Driver"
            username="compound"
            password="asdf"
            pooled = true
            logSql = false


            properties {

                jmxEnabled = true
                initialSize = 5

                //quartz threads + 10!
                maxActive = 50
                minIdle = 5
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
                jdbcInterceptors = "ConnectionState;StatementCache(max=200)"
                defaultTransactionIsolation = java.sql.Connection.TRANSACTION_READ_COMMITTED
                removeAbandoned = true
                logAbandoned = true
            }
        }

    }
}
