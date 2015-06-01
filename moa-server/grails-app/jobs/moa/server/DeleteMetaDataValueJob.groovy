package moa.server

import curation.rules.spectra.RemoveIdenticalSpectraRule
import grails.converters.JSON
import moa.MetaDataValue
import moa.server.metadata.MetaDataPersistenceService
import moa.server.query.SpectraQueryService
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 6/1/15
 * Time: 1:39 PM
 */
class DeleteMetaDataValueJob {

    def max = 100

    def group = "delete"

    def description = "removes deleted metadata from the system"

    static triggers = {
        cron name: 'deleteMetadataValues', cronExpression: '0 */1 * * * ?', priority: 10

    }

    MetaDataPersistenceService metaDataPersistenceService

    def execute(context) {

        Map data = context.mergedJobDataMap

        if (data != null) {

            def list = []
            MetaDataValue.findAllByDeleted(true,[max:max]).each {

                list.add(it)
            }

            list.each {
                metaDataPersistenceService.removeMetaDataValue(it,true)
            }
        } else {
            log.warn("no data were provided")
        }
    }
}
