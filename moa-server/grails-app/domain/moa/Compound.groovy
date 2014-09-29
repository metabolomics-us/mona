package moa

class Compound extends SupportsMetaData{

    static constraints = {
        inchiKey unique: true, nullable: false
        molFile nullable: true
    }

    static mapping = {
        inchiKey index: true, indexAttributes: [unique: true]
        molFile sqlType: "text"
        version false
        names fetch: 'join'

    }
    /**
     * this compound belongs to one spectrum
     */
    static hasMany = [names: Name]

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

}
