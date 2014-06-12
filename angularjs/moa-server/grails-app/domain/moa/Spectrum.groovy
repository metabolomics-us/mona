package moa

class Spectrum {
    static mapWith = "mongo"

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
            submitter: Submitter,
            chemicalCompound: Compound,
            biologicalCompound: Compound
    ]

    static constraints = {
        comments nullable: true
        spectrum nullable: false, unique: true
    }

    static mapping = {
        comments sqlType: "text"
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
}
