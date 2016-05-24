package moa.server.query

import grails.converters.JSON
import moa.query.Query
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Created by sajjan on 10/25/15.
 */
class PredefinedQueryService {
    SpectraQueryService spectraQueryService

    def updateQueryCounts() {
        def queries = Query.findAll()

        queries.each {
            it.queryCount = spectraQueryService.getCountForQuery(JSON.parse(it.query))
            it.save(flush: true)
        }
    }
}