package moa.server

import util.FireJobs

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 9/28/15
 * Time: 1:44 PM
 */
class DumpDataJob {


    def concurrent = false

    def group = "export"

    def description = "schedules repository dumps"

    static triggers = {
        cron name: 'updateRepository', startDelay: 60, cronExpression: '0 0 0 ? * SAT', priority: 10
    }

    def execute() {
        FireJobs.fireSpectraRepositoryExportJob([:])
    }
}
