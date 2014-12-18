package moa

class Compound extends SupportsMetaData {

    Date dateCreated
    Date lastUpdated

    static constraints = {
        inchiKey unique: true, nullable: false
        molFile nullable: true
        inchi nullable: true
    }

    static mapping = {
        inchiKey index: true, indexAttributes: [unique: true]
        molFile sqlType: "text"
        version false
        names fetch: 'join'
        inchi sqlType: "text"
    }
    /**
     * this compound belongs to one spectrum
     */
    static hasMany = [names: Name, tags:Tag, comments:Comment]

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
     * associated tags
     */
    Set<Tag> tags

    /**
     * assoicated comments
     */
    Set<Comment> comments

    /**
     * associated inchi code
     */
    String inchi
}
