package moa.server

class HibernateStatisticsFilters {
    def sessionFactory

    def filters = {

// ...

        logHibernateStats(controller: '*', action: '*') {
            before = {
                def stats = sessionFactory.getStatistics()

                log.debug "\n### In action: $controllerName/$actionName ###"

                if (!stats.statisticsEnabled) {
                    stats.statisticsEnabled = true
                }
            }

            afterView = {
                def stats = sessionFactory.getStatistics()

                log.debug """
            ############## Hibernate Stats ##############
            Action: /${controllerName}/${actionName}

            Transaction Count: ${stats.transactionCount}
            Flush Count: ${stats.flushCount}
            Prepared Statement Count: ${stats.prepareStatementCount}
            #############################################
        """

                stats.clear() // We assume no one else is using stats
            }
        }
    }
}
