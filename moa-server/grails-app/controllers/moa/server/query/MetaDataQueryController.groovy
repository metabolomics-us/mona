package moa.server.query

import grails.converters.JSON

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

        log.info(json)

        def valid = metaDataQueryService.validateQuery(json as HashMap)

        // metadata query is valid
        if(valid.success) {
            if (json.query) {

                result = metaDataQueryService.query(json.query, params)

            } else {
                result = metaDataQueryService.query(json, params)
            }
        }
        // we have a malformed metadata query.. show where the problems are
        else {
            valid.each { result.add(it.message) }
            response.status = 400
        }

        render(result as JSON)
    }
}
