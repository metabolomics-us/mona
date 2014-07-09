package moa.persistence

import grails.rest.RestfulController
import moa.MetaDataValue

/**
 * Created by wohlgemuth on 7/3/14.
 */
class MetaDataValueController extends RestfulController<MetaDataValue> {

    static responseFormats = ['json']


    def beforeInterceptor = {
        log.info(params)
    }

    public MetaDataValueController() {
        super(MetaDataValue,true)
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

    protected MetaDataValue queryForResource(Serializable id) {
        return resource.get(id)
    }

    protected List<MetaDataValue> listAllResources(Map params) {

        return MetaDataValue.createCriteria().list(params) {

            //if we have a metadata id
            if (params.MetaDataId) {
                metaData {
                    eq("id", Long.parseLong(params.MetaDataId))

                    //if we also have a category
                    if (params.MetaDataCategoryId) {
                        category {
                            eq("id", Long.parseLong(params.MetaDataCategoryId))
                        }

                    }
                }
            }

        }
    }

}
