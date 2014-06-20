package moa

class Tag {

    static constraints = {
        text(blank: false, unique: true)
    }

    static mapping = {
        text index: true, indexAttributes: [unique: true]
    }

    /**
     * name of tag
     */
    String text
}
