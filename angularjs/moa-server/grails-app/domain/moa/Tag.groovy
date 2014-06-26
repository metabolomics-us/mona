package moa

class Tag {

    static constraints = {
	    text blank: false, unique: true, nullable: false
    }

    static mapping = {
        text index: true, indexAttributes: [unique: true]
        version false
    }

    /**
     * name of tag
     */
    String text
}
