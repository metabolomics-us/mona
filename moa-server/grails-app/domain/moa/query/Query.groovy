package moa.query

import grails.converters.JSON
import moa.SpectrumQueryDownload
import util.FireJobs

/**
 * defienes a pre defined query for easy usability
 */
class Query {
    def spectraQueryService

    Date dateCreated
    Date lastUpdated

    static constraints = {
        query nullable: false
        label nullable: false
        description nullable: false
        jsonExport nullable: true
        mspExport nullable: true
    }

    static mapping = {
        version false
        query sqlType: "text"
        description sqlType: "text"
        label unique: false
    }

    /**
     * the actual query to execute
     */
    String query

    /**
     * a label for our query, to easy find it again
     */
    String label

    /**
     * a description for our query
     */
    String description

    /**
     * count of spectra that satisfy the corresponding query
     */
    int queryCount

    /**
     * associated query export in JSON format
     */
    SpectrumQueryDownload jsonExport

    /**
     * associated query export in MSP format
     */
    SpectrumQueryDownload mspExport


    def beforeInsert() {
        queryCount = spectraQueryService.getCountForQuery(JSON.parse(query))
    }

    def afterInsert() {
        // Generate JSON export
        FireJobs.fireSpectraQueryExportJob([query: query, label: label])

        // Generate MSP export
        FireJobs.fireSpectraQueryExportJob([query: query, label: label, format: "msp"])
    }
}
