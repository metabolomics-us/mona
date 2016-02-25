package moa

import curation.scoring.Scoreable
import moa.scoring.Score
import moa.splash.Splash
import util.chemical.CompoundType

class Spectrum extends SupportsMetaData implements Scoreable {

    static transients = [
            "spectrum",
            "hash",
            "queryOptions",
            "chemicalCompound",
            "biologicalCompound",
            "predictedCompound"
    ]

    Date dateCreated
    Date lastUpdated

    /**
     * contains one biological compound and one chemical compound
     */
    static hasOne = [
            submitter: Submitter,
            splash   : Splash
    ]

    /**
     * contains many metadata
     */
    static hasMany = [
            comments     : Comment,
            ions         : Ion,
            compoundLinks: CompoundLink
    ]

    /**
     * we belong to these
     */
    static belongsTo = [
            submitter: Submitter
    ]

    static constraints = {
        comments nullable: true
        submitter nullable: true
        score nullable: true
        library nullable: true
        splash nullable: true
        deleted nullable: true
        compoundLinks nullable: false
    }

    static mapping = {

        //batchSize(50)
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
    List<Comment> comments

    /**
     * compound links
     */
    Set<CompoundLink> compoundLinks

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
     * Origin information for this spectrum
     */
    Library library

    /**
     * Spectrum identifier from the origin
     * @param compound
     */
    String libraryIdentifier



    void setChemicalCompound(Compound compound) {
        CompoundLink link = new CompoundLink()
        link.compound = compound
        link.spectrum = this
        link.type = CompoundType.CHEMICAL

        addToCompoundLinks(link)
    }

    Compound getChemicalCompound() {
        Compound compound = null

        getCompoundLinks().each {
            if (it.type.equals(CompoundType.CHEMICAL)) {
                compound = it.compound
                return true
            }
        }

        return compound
    }

    void setBiologicalCompound(Compound compound) {
        CompoundLink link = new CompoundLink()
        link.compound = compound
        link.spectrum = this
        link.type = CompoundType.BIOLOGICAL

        addToCompoundLinks(link)
    }

    Compound getBiologicalCompound() {
        Compound compound = null

        getCompoundLinks().each {
            if (it.type.equals(CompoundType.BIOLOGICAL)) {
                compound = it.compound
                return true
            }
        }

        return compound
    }

    void setPredictedCompound(Compound compound) {
        CompoundLink link = new CompoundLink()
        link.compound = compound
        link.spectrum = this
        link.type = CompoundType.PREDICTED


        addToCompoundLinks(link)
    }

    Compound getPredictedCompound() {
        Compound compound = null

        getCompoundLinks().each {
            if (it.type.equals(CompoundType.PREDICTED)) {
                compound = it.compound
                return true
            }
        }

        return compound
    }

    /**
     * related splash code
     */
    Splash splash

    /**
     * has this spectra been deleted
     */
    Boolean deleted

    /**
     * quick access method to not change existing format
     * @return
     */
    String getHash() {
        if (splash != null) {
            return splash.getSplash();

        }

        return "not yet calculated!"
    }

    Map queryOptions = [:]

    def addQueryOption(def key, def value) {
        queryOptions.put(key, value)
    }

    /**
     * set deleted to false as default value
     * @return
     */
    def beforeValidate() {
        if (deleted == null) {
            deleted = false
        }
    }
}
