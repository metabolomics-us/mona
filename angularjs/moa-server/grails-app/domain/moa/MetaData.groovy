package moa

class MetaData {

    static constraints = {
        name blank: false
        values nullable: true
    }

    static mapping = {
        version false
    }

    static hasMany = [values:Value]
    /**
     * the key for this metadata
     */
    String name

    /**
     * associated content
     */
    Set<Value> values

}
