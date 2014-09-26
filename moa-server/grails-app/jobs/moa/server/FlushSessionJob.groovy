package moa.server

import moa.Spectrum

/**
 * simple job to clear the session all 5 minutes
 */
class FlushSessionJob {

    def propertyInstanceMap = org.codehaus.groovy.grails.plugins.DomainClassGrailsPlugin.PROPERTY_INSTANCE_MAP

    def description = "flushes and clears the hibernate session to keep performance up for mass inserts/updates"
    static triggers = {
        cron name: 'flushSession', startDelay: 1000000, cronExpression: '0 */2 * * * ?'
    }

    def execute() {

        Spectrum.withSession { session ->
            log.info("flushing session to keep speed up....")
            session.flush()
            session.clear()
            propertyInstanceMap.get().clear()

            log.info("\t=>\tsession is cleared")
        }
    }
}
