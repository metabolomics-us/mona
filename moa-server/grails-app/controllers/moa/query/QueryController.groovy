package moa.query

import grails.converters.JSON
import grails.rest.RestfulController

class QueryController extends RestfulController<Query> {
    static responseFormats = ['json']

    QueryController() {
        super(Query.class)
    }

    def getQueryByLabel(){
        render Query.findAllByLabel(params.label) as JSON
    }
}
