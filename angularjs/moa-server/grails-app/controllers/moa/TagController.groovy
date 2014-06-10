package moa

import grails.rest.RestfulController

class TagController extends RestfulController {
    static responseFormats = ['json']

    public TagController() {
        super(Tag)
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
