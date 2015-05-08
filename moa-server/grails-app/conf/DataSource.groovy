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
//	        url = "jdbc:postgresql://localhost:5432/mona-test"


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
            dbCreate = "update"
            url = "jdbc:postgresql://venus.fiehnlab.ucdavis.edu:5432/moa-prod"
        }
    }

}
