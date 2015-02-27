package moa

class Compound extends SupportsMetaData {

    Date dateCreated
    Date lastUpdated

    static constraints = {
        inchiKey unique: true, nullable: false
        molFile nullable: false
        inchi nullable: false
    }

    static mapping = {
        inchiKey index: true, indexAttributes: [unique: true]
        molFile sqlType: "text"
        version false
        names batchSize: 20
        inchi sqlType: "text"
    }
    /**
     * this compound belongs to one spectrum
     */
    static hasMany = [names: Name, comments:Comment]

    /**
     * inchiKey of this compound
     */
    String inchiKey

    /**
     * associated mol file for this compound to avoid excessive requests
     */
    String molFile

    /**
     * names associated to this compound
     */
    Set<Name> names

    /**
     * assoicated comments
     */
    Set<Comment> comments

    /**
     * associated inchi code
     */
    String inchi

}
