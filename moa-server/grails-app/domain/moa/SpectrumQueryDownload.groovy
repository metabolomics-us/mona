package moa

/**
 * Created by sajjan on 7/28/15.
 */
class SpectrumQueryDownload {
    Date dateCreated
    Date lastUpdated

    static constraints = {
    }

    static mapping = {
        emailAddress sqlType: "text"
        query sqlType: "text"
        label sqlType: "text"
        version false
    }

    String emailAddress
    String queryFile
    String exportFile
    String query
    String label
}
