package moa

class Spectrum extends SupportsMetaData {

    Date dateCreated
    Date lastUpdated

    /**
     * contains one biological compound and one chemical compound
     */
    static hasOne = [
            submitter: Submitter
    ]

    /**
     * contains many metadata
     */
    static hasMany = [ tags: Tag, comments:Comment]

    /**
     * we belong to these
     */
    static belongsTo = [
            submitter         : Submitter,
            chemicalCompound  : Compound,
            biologicalCompound: Compound,
            predictedCompound: Compound
    ]

    static constraints = {
	    comments nullable: true
	    spectrum nullable: false //, unique: true
        chemicalCompound nullable: true
        biologicalCompound nullable: true
        predictedCompound nullable: true
        submitter nullable: true
    }

    static mapping = {
        spectrum sqlType: "text"
        version false
        tags fetch: 'join'
        comments fetch: 'join'
    }

    /**
     * raw data: (m/z, intensity) pairs
     */
    String spectrum

    /**
     * comments
     */
    Set<Comment> comments

    /**
     * tags
     */
    Set<Tag> tags

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
     * a predicted possible compound by internal algorithms, based on available data
     */
    Compound predictedCompound
}
