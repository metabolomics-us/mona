package moa.server.query

import moa.query.Query

/**
 * Created by sajjan on 10/25/15.
 */
class PredefinedQueryService {
    SpectraQueryService spectraQueryService

    def updateQueryCounts() {
        def queries = Query.findAll()

        queries.each {
            it.count = spectraQueryService.getCountForQuery(it.query)
            it.save(flush: true)
        }
    }
}