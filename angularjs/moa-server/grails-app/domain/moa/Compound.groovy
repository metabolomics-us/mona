package moa

import grails.rest.Resource

@Resource(uri="/rest/compound", formats=['json'])
class Compound {
	// mongo injection
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
