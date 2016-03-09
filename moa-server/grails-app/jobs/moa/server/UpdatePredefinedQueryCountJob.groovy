package moa.server

import moa.server.query.PredefinedQueryService

/**
 * Created by sajjan on 10/1/15.
 */
class UpdatePredefinedQueryCountJob {
    def concurrent = false

    def group = "update"

    def description = "schedules an update to the spectrum counts for all predefined queries"

    static triggers = {
        cron name: 'updateQueryCounts', cronExpression: '0 0 0 * * ?', priority: 10
    }

    PredefinedQueryService predefinedQueryService

    def execute() {
        predefinedQueryService.updateQueryCounts()
    }
}
