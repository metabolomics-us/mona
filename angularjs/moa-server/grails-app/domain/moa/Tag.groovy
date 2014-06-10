package moa

class Tag {
	// mongo injection
	def mongo

    static constraints = {
        text(blank: false)
    }

    /**
     * name of tag
     */
    String text
}
