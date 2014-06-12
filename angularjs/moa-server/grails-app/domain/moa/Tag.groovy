package moa

class Tag {
	// mongo injection
	def mongo

    static constraints = {
        text(blank: false, unique: true)
    }

    /**
     * name of tag
     */
    String text
}
