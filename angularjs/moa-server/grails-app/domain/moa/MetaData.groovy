package moa

import grails.rest.Resource

@Resource()
class MetaData {
	// mongo injection
	def mongo

    static constraints = {
    }

	/**
	 * the spectrum this metadata belongs to
	 */
	static belongsTo = [spectrum:Spectrum]

	/**
	 * the key for this metadata
	 */
	String key

	/**
	 * the actual value of this metadata
	 */
	String value

}
