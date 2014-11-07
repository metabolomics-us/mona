package moa.persistence

import grails.converters.JSON
import grails.rest.RestfulController
import moa.Spectrum
import moa.server.SpectraPersistenceService
import moa.server.SpectraUploadJob
import moa.server.convert.SpectraConversionService
import moa.server.query.SpectraQueryService

class SpectrumController extends RestfulController<Spectrum> {
    static responseFormats = ['json']

    SpectraPersistenceService spectraPersistenceService

    SpectraQueryService spectraQueryService

    SpectraConversionService spectraConversionService

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

        def spectrum = spectraQueryService.query(params.id as long)

        switch (params.format) {
            case "msp":
                response.setContentType("application/octet-stream")
                response.setHeader("Content-disposition", "attachment;filename=${params.id}.msp")
                response.outputStream << spectraConversionService.convertToMsp(spectrum)
                response.outputStream.flush()
                break
            case "mona":
                response.setContentType("application/octet-stream")
                response.setHeader("Content-disposition", "attachment;filename=${params.id}.json")

                response.outputStream << (spectrum as JSON)
                response.outputStream.flush()
                break
            default:
                render spectrum as JSON
        }
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

        query.metadata = []

        //if a category is specified
        if (params.MetaDataId) {

            def object = [id: params.MetaDataId]

            //if we also have a category

            /**
             *
             * IGNORE FOR NOW, SICNE IT AINT WORKING
             if (params.MetaDataCategoryId) {object.category = [:]

             object.category.id = params.MetaDataCategoryId}**/

            query.metadata.add(object)
        } else if (params.MetaDataCategoryId) {
            def object = [:]

            object.category = [:]

            object.category.id = params.MetaDataCategoryId


            query.metadata.add(object)
        }



        log.info("build query: ${query as JSON}")
        return spectraQueryService.query(query, params)
    }


}
