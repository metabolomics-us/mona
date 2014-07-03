package moa.persistence

import grails.rest.RestfulController
import moa.MetaData

class MetaDataController extends RestfulController<MetaData> {

    static responseFormats = ['json']


    def beforeInterceptor = {
        log.info(params)
    }

    public MetaDataController() {
        super(MetaData)
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
        log.info(params)
        if (params.MetaDataCategoryId) {
            return MetaData.findAllByCategory(params.MetaDataCategoryId)
        } else {
            return resource.get(id)
        }
    }

    protected List<MetaData> listAllResources(Map params) {

        return MetaData.createCriteria().list {

            if (params.MetaDataCategoryId) {
                category {
                    eq("id", Long.parseLong(params.MetaDataCategoryId))
                }

            }
        }
    }

}
