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

        threadPool.'class' = 'org.quartz.simpl.SimpleThreadPool'
        //use n-2 threads or 2 otherwise
        threadPool.threadCount = Runtime.getRuntime().availableProcessors() > 2 ? Runtime.getRuntime().availableProcessors() -2 : 2
        threadPool.threadPriority = 7

        jobStore.misfireThreshold = 60000

        jobStore.'class' = 'org.quartz.impl.jdbcjobstore.JobStoreTX'
        jobStore.driverDelegateClass = 'org.quartz.impl.jdbcjobstore.PostgreSQLDelegate'

        jobStore.useProperties = false
        jobStore.tablePrefix = 'QRTZ_'
        jobStore.isClustered = true
        jobStore.clusterCheckinInterval = 1000

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
}
