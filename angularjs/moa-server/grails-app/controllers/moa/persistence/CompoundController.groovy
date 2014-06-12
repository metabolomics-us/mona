package moa.persistence

import grails.rest.RestfulController
import moa.Compound

class CompoundController extends RestfulController {

    static responseFormats = ['json']

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

        params
    }

    @Override
    protected Compound createResource(Map params) {
        Compound c = super.createResource(params)

        Set<String> names = c.names
        c = Compound.findOrCreateWhere(inchiKey: c.inchiKey)

        for (String s : names) {
            c.names.add(s)
        }

        return c;
    }
}
