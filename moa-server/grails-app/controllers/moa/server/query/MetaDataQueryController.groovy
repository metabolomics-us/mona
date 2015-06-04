package moa.server.query

import grails.converters.JSON
import moa.MetaData
import moa.MetaDataValue

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

        def valid = true//metaDataQueryService.validateQuery(json as HashMap)

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

    /**
     * provides a list of metadata objects suitable for queries
     */
    def listMetaDataForQueries(){

        render (
                MetaData.createCriteria().list(params){

                    if(params.name){
                        ilike("name","%${params.name}%")
                    }

                    category {
                        ne("name", "annotation")
                    }

                    eq("hidden",false)
                    eq("searchable",true)

                    order("name", "asc")

                } as JSON
        )
    }

    /**
     * a list of value for the given name, can be limited
     * @return
     */
    def listValuesForMetaDataName(){

        def list =
                MetaDataValue.createCriteria().list(){

                    metaData {
                        eq("name", params.name)

                        category {
                            ne("name", "annotation")
                        }
                    }

                    eq("deleted",false)

                    order("name", "asc")

                }

        if(params.max) {
            if (list.size() > params.max) {
                list = list[0..params.max]
            }

        }
        render (

                list as JSON
        )
    }
}
