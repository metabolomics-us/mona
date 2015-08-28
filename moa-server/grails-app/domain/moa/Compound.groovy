package moa

import util.chemical.CompoundType

class Compound extends SupportsMetaData {

    Date dateCreated
    Date lastUpdated

    static constraints = {
        inchiKey unique: true, nullable: false
        molFile nullable: false
        inchi nullable: false
       // type nullable: false //, inList: [CompoundType.BIOLOGICAL,CompoundType.CHEMICAL,CompoundType.PREDICTED]
    }

    static mapping = {
        inchiKey index: true, indexAttributes: [unique: true]
        molFile sqlType: "text"
        version false
        inchi sqlType: "text"
    }
    /**

     * this compound belongs to one spectrum
     */
    static hasMany = [names: Name, comments: Comment]

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

    /**
     * type of the compound
     */
  //  String type

}
