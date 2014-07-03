package moa.persistence
import grails.rest.RestfulController
import moa.MetaDataCategory

class MetaDataCategoryController  extends RestfulController<MetaDataCategory> {

    static responseFormats = ['json']


    def beforeInterceptor = {
        log.info(params)
    }

    public MetaDataCategoryController() {
        super(MetaDataCategory)
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

}
