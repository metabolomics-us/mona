package moa

class MetaData {

    static constraints = {
        name blank: false
    }

    static mapping = {
        version false
    }

    static hasOne = [content:Value]
    /**
     * the key for this metadata
     */
    String name

}
