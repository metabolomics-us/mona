package moa.persistence

import grails.converters.JSON
import grails.rest.RestfulController
import moa.Spectrum
import moa.server.SpectraPersistenceService
import moa.server.SpectraUploadJob

class SpectrumController extends RestfulController<Spectrum> {
    static responseFormats = ['json']

    SpectraPersistenceService spectraPersistenceService
    def beforeInterceptor = {
        //log.info(params)
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

        return Spectrum.createCriteria().list(params) {

            //if a compound is specified
            if (params.CompoundId) {
                log.info("query by compounds: ${params.CompoundId}")

                biologicalCompound {
                    eq("id", Long.parseLong(params.CompoundId))
                }

                or {
                    chemicalCompound {
                        eq("id", Long.parseLong(params.CompoundId))
                    }
                }
            }
            //if a category is specified
            if (params.MetaDataId) {

                log.info("query by meta data: ${params.MetaDataId}")
                metaData {
                    eq("id", Long.parseLong(params.MetaDataId))

                    //if we got a category as well
                }
            }
        }
    }


}
