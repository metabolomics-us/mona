package moa.server

import moa.Spectrum

/**
 * simple job to clear the session all 5 minutes
 */
class FlushSessionJob {

    def concurrent = false

    def group = "maintenance"

    def propertyInstanceMap = org.codehaus.groovy.grails.plugins.DomainClassGrailsPlugin.PROPERTY_INSTANCE_MAP

    def description = "flushes and clears the hibernate session to keep performance up for mass inserts/updates"
    static triggers = {
    //    cron name: 'flushSession', startDelay: 60, cronExpression: '0 */1 * * * ?', priority: 10
    }

    def execute() {

        Spectrum.withSession { session ->
            log.debug("flushing session")
            log.debug(" memory usage before flushing, free: ${Runtime.getRuntime().freeMemory()} total: ${Runtime.getRuntime().totalMemory()} used: ${Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()}")

            long begin = System.currentTimeMillis()
            session.flush()
            session.clear()
            propertyInstanceMap.get().clear()
            System.gc()

            long end = System.currentTimeMillis()


            log.debug("flushed session in: ${end-begin} ms")
            log.debug(" memory usage after flushing, free: ${Runtime.getRuntime().freeMemory()} total: ${Runtime.getRuntime().totalMemory()} used: ${Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()}")

        }
    }
}
