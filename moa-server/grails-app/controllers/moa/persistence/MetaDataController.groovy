package moa.persistence

import grails.rest.RestfulController
import moa.MetaData
import org.codehaus.groovy.grails.web.servlet.HttpHeaders

import static org.springframework.http.HttpStatus.OK

//@Cacheable("metadata")
class MetaDataController extends RestfulController<MetaData> {

    static responseFormats = ['json']


    def beforeInterceptor = {
    }

    public MetaDataController() {
        super(MetaData, false)
    }

    /**
     * otherwise grails won't populate the json fields
     * @return
     */
    protected Map getParametersToBind() {
             /*

if we add the json it breaks the update function of the rest controller

        if (request.JSON) {
            params.putAll(
                    request.JSON)
        }
               */
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

            eq("hidden",false)
        }
    }

}
