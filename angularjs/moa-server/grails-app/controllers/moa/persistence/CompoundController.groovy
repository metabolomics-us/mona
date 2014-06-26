package moa.persistence

import grails.rest.RestfulController
import moa.Compound
import moa.Name

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

        Set<Name> names = c.names
        c = Compound.findOrSaveByInchiKey(c.inchiKey)

        if(c.names == null){
            c.names = new HashSet<>();
        }
        for (Name s : names) {
            c.names.add(Name.findOrSaveByName(s.name))
        }

        return c;
    }
}
