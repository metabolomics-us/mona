package moa.persistence

import grails.converters.JSON
import grails.rest.RestfulController
import moa.Compound
import moa.Name
import moa.server.convert.CompoundConversionService

class CompoundController extends RestfulController {

    static responseFormats = ['json']

    CompoundConversionService compoundConversionService

    def beforeInterceptor = {
        log.info(params)
    }

    public CompoundController() {
        super(Compound, true)
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

        if (c.names == null) {
            c.names = new HashSet<>();
        }
        for (Name s : names) {
            c.names.add(Name.findOrSaveByName(s.name))
        }

        return c;
    }


    @Override
    def show() {

        Compound compound = queryForResource(params.id)

        switch (params.format) {
            case "mol":
                response.setContentType("application/octet-stream")
                response.setHeader("Content-disposition", "attachment;filename=${params.id}.mol")
                response.outputStream << compoundConversionService.convertToMol(compound)
                response.outputStream.flush()
                break
            case "mona":
                response.setContentType("application/octet-stream")
                response.setHeader("Content-disposition", "attachment;filename=${params.id}.json")

                response.outputStream << (compound as JSON)
                response.outputStream.flush()
                break

            case "sdf":
                response.setContentType("application/octet-stream")
                response.setHeader("Content-disposition", "attachment;filename=${params.id}.sdf")
                response.outputStream << compoundConversionService.convertToSdf(compound)
                response.outputStream.flush()
                break
            default:
                render compound as JSON
        }
    }
}
