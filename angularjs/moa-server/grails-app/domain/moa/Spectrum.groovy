package moa

class Spectrum {
    //static mapWith = "mongo"

    /**
     * contains one biological compound and one chemical compound
     */
    static hasOne = [
            submitter: Submitter
    ]

    /**
     * contains many metadata
     */
    static hasMany = [metaData: MetaData, tags: Tag]

    /**
     * we belong to these
     */
    static belongsTo = [
            submitter         : Submitter,
            chemicalCompound  : Compound,
            biologicalCompound: Compound
    ]

    static constraints = {
	    comments nullable: true, blank: true
	    spectrum nullable: false, unique: true
        chemicalCompound nullable: true
        biologicalCompound nullable: true
        submitter nullable: true
    }

    static mapping = {
        comments sqlType: "text"
        spectrum sqlType: "varchar", length: 8000
        version false
    }

    /**
     * raw data: (m/z, intensity) pairs
     */
    String spectrum

    /**
     * comments
     */
    String comments

    /**
     * who submitted this
     */
    Submitter submitter

    /**
     * bio logical compound
     */
    Compound chemicalCompound

    /**
     * chemical compound
     */
    Compound biologicalCompound

    /**
     * has this spectrum being curated
     */
    boolean curated = false


}
