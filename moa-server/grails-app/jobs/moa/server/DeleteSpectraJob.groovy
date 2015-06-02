package moa.server

import curation.rules.spectra.RemoveIdenticalSpectraRule
import grails.converters.JSON
import moa.server.query.SpectraQueryService
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 3/6/15
 * Time: 12:48 PM
 */
class DeleteSpectraJob {

    def max = 1000

    /**
     * force true forces instant deletion
     * force false marks the element as deleted
     */
    def force = true

    def concurrent = false

    def group = "delete"

    def description = "removes spectra from the system"

    static triggers = {
        cron name: 'deleteDuplicates', cronExpression: '0 */1 * * * ?', priority: 10

    }

    SpectraQueryService spectraQueryService

    def execute(context) {

        Map data = context.mergedJobDataMap

        if (data != null) {
            if (data.containsKey("deleteSpectra")) {

                def json = null

                if (data.deleteSpectra instanceof JSONObject) {

                    json = data.delete
                } else {
                    json = JSON.parse(data.deleteSpectra.toString())
                }
                log.info("calling delete service...")
                spectraQueryService.searchAndDelete(json)
                log.info("job finished!")
            } else {
                log.warn("we were missing the 'deleteSpectra' - so we delete outdated max ${max} spectra by tag")
                spectraQueryService.searchAndDelete(
                        [tags:
                                 [
                                         //we only want spectra which requires deletion
                                         [name:
                                                  [
                                                          eq: RemoveIdenticalSpectraRule.REQUIRES_DELETE
                                                  ]
                                         ]

                                 ]
                        ]
                        , [forceRemoval: force, max: max])

                if(force){
                    spectraQueryService.searchAndDelete(
                            [tags:
                                     [
                                             //we only want spectra which requires deletion
                                             [name:
                                                      [
                                                              eq: RemoveIdenticalSpectraRule.DELETED
                                                      ]
                                             ]

                                     ]
                            ]
                            , [forceRemoval: force, max: max])

                }
            }
        } else {
            log.warn("no data were provided")
        }
    }
}
