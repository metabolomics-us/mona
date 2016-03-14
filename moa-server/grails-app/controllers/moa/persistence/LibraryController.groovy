package moa.persistence

import grails.rest.RestfulController
import moa.Library

class LibraryController extends RestfulController {

    static responseFormats = ['json']

    public LibraryController() {
        super(Library)
    }

    @Override
    protected Map getParametersToBind() {
        if (request.JSON) {
            params.putAll(request.JSON)
        }

        params
    }
}
