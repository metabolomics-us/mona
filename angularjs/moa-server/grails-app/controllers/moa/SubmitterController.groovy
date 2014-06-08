package moa

import grails.rest.RestfulController

class SubmitterController extends RestfulController {

    static responseFormats = ['json']

    public SubmitterController() {
        super(Submitter)
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
