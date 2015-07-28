package moa

import curation.scoring.Scoreable
import moa.scoring.Score
import moa.splash.Splash

class Spectrum extends SupportsMetaData implements Scoreable {

    static transients = ["spectrum","hash"]

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
            ions    : Ion
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

    /**
     * returns a sorted spectra for us
     * @return
     */
    String getSpectrum() {
        if (ions == null) {
            return ""
        }
        return ions.sort(false, new Comparator<Ion>() {
            @Override
            int compare(Ion o1, Ion o2) {
                return o1.getMass().compareTo(o2.getMass())
            }
        }).join(" ")
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
     * related splash code
     */
    Splash splash

    /**
     * quick access method to not change existing format
     * @return
     */
    String getHash(){
        if(splash != null){
            return  splash.getSplash();

        }

        return "not yet calculated!"
    }
}
