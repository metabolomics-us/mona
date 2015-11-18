package moa.query

import moa.SpectrumQueryDownload

/**
 * defienes a pre defined query for easy usability
 */
class Query {

    Date dateCreated
    Date lastUpdated

    static constraints = {
        query nullable: false
        label nullable: false
        description nullable: false
        queryExport nullable: true
    }

    static mapping = {
        version false
        query sqlType: "text"
        description sqlType: "text"
        label unique: false
        queryCount defaultValue: "0"
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
     * associated query export
     */
    SpectrumQueryDownload queryExport


    def afterUpdate() {

    }
}
