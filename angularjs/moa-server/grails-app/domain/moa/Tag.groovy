package moa

class Tag {

    static constraints = {
        text(blank: false)
    }

    /**
     * name of tag
     */
    String text
}
