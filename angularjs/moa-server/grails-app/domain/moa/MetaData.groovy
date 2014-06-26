package moa

class MetaData {
	//static mapWith = "mongo"

	static constraints = {
		key blank: false
		value blank: false
		spectrum nullable: true
		type blank: false, nullable: false
	}

	/**
	 * the spectrum this metadata belongs to
	 */
	static belongsTo = [spectrum: Spectrum]

    static mapping = {
        version false
    }

	/**
	 * the key for this metadata
	 */
	String key

	/**
	 * the actual value of this metadata
	 */
	String value

	/**
	 * the used type. This should be a
	 */
	String type

}
