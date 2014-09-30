package moa

class Tag {

    static constraints = {
	    text blank: false, unique: true, nullable: false
        ruleBased unique: false, nullable:true
    }

    static mapping = {
        text index: true, indexAttributes: [unique: true]
    }

    /**
     * name of tag
     */
    String text

    /**
     * is this tag being applied from the rule based machine
     */
    boolean ruleBased = false
}
