package moa

class Compound {
    def mongo

    static constraints = {
        inchiKey unique: true,nullable: false
    }

    static mapping = {
        inchiKey index: true, indexAttributes: [unique: true]
    }
	/**
	 * this compound belongs to one spectrum
	 */
//	static hasMany = [spectra: Spectrum]

    /**
     * inchiKey of this compound
     */
    String inchiKey

	/**
	 * names associated to this compound
	 */
	Set<String> names = [] as Set<String>

    /**
     * associated spectra
     */
    List<Spectrum> spectra;
}
