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
    def query() {

        def result = []
        def json = request.JSON

        if (json.query) {

            result = metaDataQueryService.query(json.query, params)

        } else {
            result = metaDataQueryService.query(json, params)
        }

        render(result as JSON)
    }
}
