package moa

class Compound {
    //static mapWith = "mongo"

    static constraints = {
	    inchiKey unique: true, nullable: false
    }

    static mapping = {
        inchiKey index: true, indexAttributes: [unique: true]
        version false
    }
	/**
	 * this compound belongs to one spectrum
	 */
	static hasMany = [spectra: Spectrum]

    static  mappedBy = [spectra:'biologicalCompound']

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
	Set<String> names = [] as Set<String>

    /**
     * associated spectra
     */
    List<Spectrum> spectra;
}
