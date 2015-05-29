/**
 * shared settings
 */
dataSource {
    pooled = true
    driverClassName = "org.postgresql.Driver"
    username = "compound"
    password = "asdf"
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
            url = "jdbc:postgresql://venus.fiehnlab.ucdavis.edu:5432/moa-prod"
//	        url = "jdbc:postgresql://localhost:5432/mona-test"


            properties {
                jmxEnabled = true
                initialSize = 5
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
                jdbcInterceptors = "ConnectionState"
                defaultTransactionIsolation = java.sql.Connection.TRANSACTION_READ_COMMITTED
            }

            logSql = true

        }
    }

    test {
        dataSource {
            dbCreate = "update"// "create-drop"
            url = "jdbc:postgresql://venus.fiehnlab.ucdavis.edu:5432/moa-devel"
            driverClassName = "org.postgresql.Driver"
            username = "compound"
            password = "asdf"
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
            url = "jdbc:postgresql://128.120.143.126:5432/moa-prod"

            /*
		 properties {
        		maxActive = 50
        		maxAge = 10 * 60000
        		timeBetweenEvictionRunsMillis = 5000
        		minEvictableIdleTimeMillis = 60000
        		numTestsPerEvictionRun=3
        		testOnBorrow=true
        		testWhileIdle=true
        		testOnReturn=true
        		validationQuery="SELECT 1"
    		}

    		*/

            logSql = false


            properties {
                jmxEnabled = true
                initialSize = 5
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
            }

        }
    }

}
