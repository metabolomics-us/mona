package moa.query

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
        count nullable: true
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
     * number of spectra associated with this query
     */
    int count
}
