package moa.persistence

import grails.rest.RestfulController
import moa.MetaData
import org.springframework.cache.annotation.Cacheable

@Cacheable("metadata")
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
        log.info(params)

        if (request.JSON) {
            params.putAll(
                    request.JSON)
        }

        params
    }


    protected MetaData queryForResource(Serializable id) {
        return resource.get(id)
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
