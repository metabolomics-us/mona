package moa.persistence

import grails.rest.RestfulController
import moa.Tag

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
        params
    }
}
