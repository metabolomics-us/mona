package moa.server

import grails.converters.JSON
import moa.server.query.SpectraQueryService
import net.minidev.json.JSONObject

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 3/6/15
 * Time: 12:48 PM
 */
class DeleteSpectraJob {
    SpectraQueryService spectraQueryService

    def concurrent = false


    def group = "delete"

    def description = "removes spectra from the system"
    static triggers = {
        cron name: 'news', startDelay: 60, cronExpression: '0 */1 * * * ?', priority: 10
    }

    def execute(context) {

        Map data = context.mergedJobDataMap

        if (data != null) {
            if(data.containsKey("deleteSpectra")){

                def json = null

                if(data.deleteSpectra instanceof JSONObject){

                    json = data.delete
                }
                else{
                    json = JSON.parse(data.deleteSpectra.toString())
                }
                spectraQueryService.delete(json)
            }
        }
    }
}
