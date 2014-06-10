package moa

class Tag {
	// mongo injection
	def mongo

	/**
	 * tag value
	 */
	String value

	static constraints = {
		value blank:false
	}
}
