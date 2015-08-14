package moa.server

import curation.rules.spectra.RemoveIdenticalSpectraRule
import grails.converters.JSON
import groovy.sql.Sql
import moa.MetaDataValue
import moa.server.metadata.MetaDataPersistenceService
import moa.server.query.SpectraQueryService
import org.codehaus.groovy.grails.web.json.JSONObject

import javax.sql.DataSource

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 6/1/15
 * Time: 1:39 PM
 */
class DeleteMetaDataValueJob {


    def group = "delete"

    def description = "removes deleted metadata from the system"

    def concurrent = false

    static triggers = {
        cron name: 'deleteMetadataValues', cronExpression: '0 */10 * * * ?', priority: 10

    }

    MetaDataPersistenceService metaDataPersistenceService

    def execute(context) {

        Map data = context.mergedJobDataMap

        if (data != null) {
            MetaDataValue.executeUpdate("delete from MetaDataValue where deleted = true")
        } else {
            log.warn("no data were provided")
        }
    }
}
