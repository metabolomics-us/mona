package moa.persistence

import grails.rest.RestfulController
import moa.Compound

class CompoundController extends RestfulController {

    static responseFormats = ['json']

    def index() {}

    public CompoundController() {
        super(Compound)
    }

    /**
     * otherwise grails won't populate the json fields
     * @return
     */
    protected Map getParametersToBind() {
        if (request.JSON) {
            params.putAll(
                    request.JSON)
        }

        println "modified params: ${params}"
        params
    }
}
