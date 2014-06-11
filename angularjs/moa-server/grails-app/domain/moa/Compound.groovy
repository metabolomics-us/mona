package moa

import grails.rest.Resource

@Resource()
class Compound {
    def mongo


    static constraints = {
    }

	/**
	 * this compound belongs to one spectrum
	 */
	static belongsTo = [spectrum: Spectrum]

    /**
     * inchiKey of this compound
     */
    String inchiKey

    /**
     * inchiCode of this compound
     */
    String inchiCode

	/**
	 * names associated to this compound
	 */
	List<String> names
}
