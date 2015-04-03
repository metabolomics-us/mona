package moa

import curation.scoring.Scoreable
import moa.scoring.Score

class Spectrum extends SupportsMetaData implements Scoreable{

    static transients = [ "spectrum" ]

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
    static hasMany = [ comments:Comment, ions:Ion]

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
        chemicalCompound nullable: true
        biologicalCompound nullable: true
        predictedCompound nullable: true
        submitter nullable: true
        score nullable: true
    }

    static mapping = {
        spectrum sqlType: "text"
        version false
        tags batchSize: 20
        comments batchSize: 20,  cascade: 'all-delete-orphan'
    }

    /**
     * raw data: (m/z, intensity) pairs
     */
    //String spectrum

    String getSpectrum(){
        if(ions == null){
            return ""
        }
        return ions.join(" ")
    }

    void setSpectrum(String spectrum){

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

}
