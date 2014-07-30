package moa.query

import grails.converters.JSON
import moa.server.query.MetaDataQueryService

/**
 * simple autocomplete controller
 */
class MetaDataQueryController {

    MetaDataQueryService metaDataQueryService

    static responseFormats = ['json']

    /**
     * lists all metadata values by search term
     */
    def query(){

        def result = metaDataQueryService.queryMetaData(request.JSON,params)

        render (result as JSON)
    }
}
