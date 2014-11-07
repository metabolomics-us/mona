package moa.persistence
import grails.rest.RestfulController
import moa.MetaData
//@Cacheable("metadata")
class MetaDataController extends RestfulController<MetaData> {

    static responseFormats = ['json']


    def beforeInterceptor = {
        log.info(params)
    }

    public MetaDataController() {
        super(MetaData, true)
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


    protected MetaData queryForResource(Serializable id) {

        MetaData metaData = resource.get(id)

        return metaData
    }

    protected List<MetaData> listAllResources(Map params) {


        return MetaData.createCriteria().list(params) {

            if (params.MetaDataCategoryId) {
                category {
                    eq("id", Long.parseLong(params.MetaDataCategoryId))
                }

            }
        }
    }

}
