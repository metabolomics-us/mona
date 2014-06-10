package moa

import grails.rest.Resource

@Resource()
class Spectrum {
	// mongo injection
	def mongo
    /**
     * contains one biological compound and one chemical compound
     */
    static hasOne = [compoundBio:Compound, compoundChem:Compound]

	/**
	 * contains many metadata
	 */
	static hasMany = [metaData:MetaData, tags:Tag]

	/**
	 * mapping of foreign keys
	 */
	static mappedBy = [compoundBio:"none", compoundChem:"none"]

    static constraints = {
    }

	static mapping = {
		comments sqlType: "text"
	}

	/**
	 * raw data: (m/z, intensity) pairs
	 */
	String spectrum

	/**
	 * comments
	 */
	String comments
}
