package moa.persistence

import grails.converters.JSON
import grails.rest.RestfulController
import moa.Spectrum
import moa.server.SpectraPersistenceService
import moa.server.SpectraUploadJob
import moa.server.query.SpectraQueryService

class SpectrumController extends RestfulController<Spectrum> {
    static responseFormats = ['json']

    SpectraPersistenceService spectraPersistenceService

    SpectraQueryService spectraQueryService

    def beforeInterceptor = {
    }

    public SpectrumController() {
        super(Spectrum)
    }

    protected Map getParametersToBind() {

        if (request.JSON) {
            params.putAll(
                    request.JSON)
        }

        params
    }

    @Override
    def show() {
        render spectraQueryService.query(params.id as long) as JSON
    }

    @Override
    protected Spectrum createResource(Map params) {
        return spectraPersistenceService.create(params)
    }

    def batchSave() {
        SpectraUploadJob.triggerNow([spectra: getParametersToBind()])

        render([message: "spectra submitted"] as JSON)
    }
    /**
     * dynamic query methods to deal with different url mappings based on mapping ids
     * @param params
     * @return
     */
    protected List<Spectrum> listAllResources(Map params) {

        log.info("params: ${params}")

        def query = [:]

        //if a compound is specified
        if (params.CompoundId) {

            query.compound = [:]
            query.compound.id = params.CompoundId

        }

        //if a category is specified
        if (params.MetaDataId) {

            query.metadata = []
            query.metadata.add( [id:params.MetaDataId]   )
        }

        return spectraQueryService.query(query, params)
    }


}
