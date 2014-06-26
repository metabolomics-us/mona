package moa

class Compound {
    //static mapWith = "mongo"

    static constraints = {
	    inchiKey unique: true, nullable: false
        molFile nullable: true
    }

    static mapping = {
        inchiKey index: true, indexAttributes: [unique: true]
        version false
    }
	/**
	 * this compound belongs to one spectrum
	 */
	static hasMany = [ names:Name]

    //static  mappedBy = [spectra:'biologicalCompound']

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

}
