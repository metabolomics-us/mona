/*
quartz {
    autoStartup = true
    jdbcStore = true
    waitForJobsToCompleteOnShutdown = true
    exposeSchedulerInRepository = false

    props {
        scheduler.skipUpdateCheck = true
    }
}

*/

quartz {
    autoStartup = true
    jdbcStore = true
    waitForJobsToCompleteOnShutdown = true

// Allows monitoring in Java Melody (if you have the java melody plugin installed in your grails app)
    exposeSchedulerInRepository = true

    props {
        scheduler.skipUpdateCheck = true
        scheduler.instanceName = 'mon'
        scheduler.instanceId = 'AUTO'
        scheduler.idleWaitTime = 1000
        scheduler.makeSchedulerThreadDaemon = true

        threadPool.'class' = 'org.quartz.simpl.SimpleThreadPool'
        //use n-2 threads or 2 otherwise
        threadPool.threadCount = 4
        //threadPool.threadPriority = 7
        threadPool.makeThreadsDaemons = true

        jobStore.misfireThreshold = 60000

        jobStore.'class' = 'org.quartz.impl.jdbcjobstore.JobStoreTX'
        jobStore.driverDelegateClass = 'org.quartz.impl.jdbcjobstore.PostgreSQLDelegate'

        jobStore.useProperties = false
        jobStore.tablePrefix = 'QRTZ_'
        jobStore.isClustered = true
        jobStore.clusterCheckinInterval = 15000

        jobStore.txIsolationLevelSerializable = true

        plugin.shutdownhook.'class' = 'org.quartz.plugins.management.ShutdownHookPlugin'
        plugin.shutdownhook.cleanShutdown = true

    }
}

environments {
    test {
        quartz {
            autoStartup = false
        }
    }
    development {
        quartz {
            autoStartup = true
        }
    }

}
