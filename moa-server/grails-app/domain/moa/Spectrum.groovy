package moa

import curation.scoring.Scoreable
import moa.scoring.Score

class Spectrum extends SupportsMetaData implements Scoreable {

    static transients = ["spectrum"]

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
    static hasMany = [
            comments: Comment,
            ions: Ion
    ]

    /**
     * we belong to these
     */
    static belongsTo = [
            submitter         : Submitter,
            chemicalCompound  : Compound,
            biologicalCompound: Compound,
            predictedCompound : Compound
    ]

    static constraints = {
        comments nullable: true
        chemicalCompound nullable: true
        biologicalCompound nullable: true
        predictedCompound nullable: true
        submitter nullable: true
        score nullable: true
        hash nullable: true
    }

    static mapping = {

        batchSize(50)
        version false
        comments /*fetch: 'join',*/ cascade: 'all-delete-orphan'
       // ions lazy:false
        //chemicalCompound lazy:false
        //biologicalCompound lazy:false
        //predictedCompound lazy:false
        //score lazy:false
        //links lazy:false
        //submitter lazy:false
    }


    String getSpectrum() {
        if (ions == null) {
            return ""
        }
        return ions.join(" ")
    }

    void setSpectrum(String spectrum) {

    }
    /**
     * comments
     */
    Set<Comment> comments

    /**
     * related ion
     */
    Set<Ion> ions

    /**
     * who submitted this
     */
    Submitter submitter

    /**
     * the score of this spectra
     */
    Score score

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

    /**
     * dedicated spectra hashcode
     */
    String hash
}
