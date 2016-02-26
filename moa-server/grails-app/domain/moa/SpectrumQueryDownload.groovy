package moa

/**
 * Created by sajjan on 7/28/15.
 */
class SpectrumQueryDownload {
    Date dateCreated
    Date lastUpdated

    static constraints = {
        emailAddress nullable: true, blank: true
        exportSize nullable: true
    }

    static mapping = {
        emailAddress sqlType: "text"
        queryFile sqlType: "text"
        exportFile sqlType: "text"
        query sqlType: "text"
        label sqlType: "text"
        version false
    }

    String emailAddress
    String queryFile
    String exportFile
    String query
    String label

    /**
     * count of spectra that satisfy the corresponding query
     */
    Integer queryCount

    /**
     * Size of export in bytes
     */
    Long exportSize
}
