package moa.persistence

import grails.converters.JSON
import grails.rest.RestfulController
import grails.transaction.Transactional
import moa.Spectrum
import moa.server.SpectraPersistenceService
import moa.server.SpectraUploadJob
import moa.server.convert.SpectraConversionService
import moa.server.query.SpectraQueryService
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject

class SpectrumController extends RestfulController<Spectrum> {
    static responseFormats = ['json']

    SpectraPersistenceService spectraPersistenceService

    SpectraQueryService spectraQueryService

    SpectraConversionService spectraConversionService

    public SpectrumController() {
        super(Spectrum)
    }

    protected Map getParametersToBind() {

        if (request.JSON) {
            params.putAll(request.JSON)
        }

        params
    }

    @Transactional
    def index(Integer max) {
        respond listAllResources(params), model: [("${resourceName}Count".toString()): countResources()]
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

    def singleSave(){

        String text = request.getReader().text

        SpectraUploadJob.triggerNow([spectra: text])
        render([message: "1 spectra submitted"] as JSON)
    }

    def batchSave() {

        if (request.JSON) {
            if (request.JSON instanceof JSONArray) {
                JSONArray array = request.JSON

                for (int i = 0; i < array.length(); i++) {
                    if (array.get(i) instanceof JSONObject) {
                        log.info("trigger uploading spectra")
                        SpectraUploadJob.triggerNow([spectra: array.getJSONObject(i).toString()])

                    }
                }

                render([message: "${array.length()} spectra submitted"] as JSON)

            } else {
                log.info("trigger uploading spectra")

                SpectraUploadJob.triggerNow([spectra: request.JSON.toString()])
                render([message: "1 spectra submitted"] as JSON)
            }
        }
        else{
            render([errors: "sorry missing JSON request"]);
        }
    }
    /**
     * dynamic query methods to deal with different url mappings based on mapping ids
     * @param params
     * @return
     */
    @Override
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
