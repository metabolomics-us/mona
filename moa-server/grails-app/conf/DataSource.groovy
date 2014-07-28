dataSource {
    pooled = true
    driverClassName = "org.h2.Driver"
    username = "sa"
    password = ""
}
hibernate {
    cache.use_second_level_cache = true
    cache.use_query_cache = false
    cache.region.factory_class = 'net.sf.ehcache.hibernate.EhCacheRegionFactory' // Hibernate 3
//    cache.region.factory_class = 'org.hibernate.cache.ehcache.EhCacheRegionFactory' // Hibernate 4
 //   format_sql = true
 //   use_sql_comments = true
}

environments {

    development {

        dataSource {
            dbCreate = /*"update"//*/"create-drop"
            url = "jdbc:postgresql://venus.fiehnlab.ucdavis.edu:5432/moa-devel"
            driverClassName="org.postgresql.Driver"
            username="compound"
            password="asdf"
            pooled = true

            cache.use_second_level_cache = false
            cache.use_query_cache = false
         //   logSql = true
        }
        /*

        dataSource {
            dbCreate = "create-drop" // one of 'create', 'create-drop', 'update', 'validate', ''
            url = "jdbc:h2:mem:devDb;MVCC=TRUE;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE"
        }
        */

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

    production {
        dataSource {
            dbCreate = "update"
            url = "jdbc:postgresql://venus.fiehnlab.ucdavis.edu:5432/moa"
            driverClassName="org.postgresql.Driver"
            username="compound"
            password="asdf"
            pooled = true
            properties {
                maxActive = -1
                minEvictableIdleTimeMillis=1800000
                timeBetweenEvictionRunsMillis=1800000
                numTestsPerEvictionRun=3
                testOnBorrow=true
                testWhileIdle=true
                testOnReturn=false
                validationQuery="SELECT 1"
                jdbcInterceptors="ConnectionState"
            }

        }
    }
}
