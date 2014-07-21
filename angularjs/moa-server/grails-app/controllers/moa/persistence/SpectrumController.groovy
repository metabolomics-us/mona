package moa.persistence
import grails.rest.RestfulController
import moa.Spectrum
import moa.server.SpectraPersistenceService

class SpectrumController extends RestfulController<Spectrum> {
    static responseFormats = ['json']

    SpectraPersistenceService spectraPersistenceService
    def beforeInterceptor = {
        log.info(params)
    }

    public SpectrumController() {
        super(Spectrum)
    }

    protected Map getParametersToBind() {
        log.info(params)

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

    protected Spectrum queryForResource(Serializable id) {

        def criteria = Spectrum.createCriteria()

        return criteria.get {
            if (params.CompoundId) {
                eq("id", Long.parseLong(id.toString()))
            }
        }
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
