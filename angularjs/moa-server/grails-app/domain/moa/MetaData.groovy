package moa


class MetaData {

    static constraints = {
        name blank: false
        value nullable: true
    }

    static mapping = {
        version false
    }

    static hasMany = [value:Value]
    /**
     * the key for this metadata
     */
    String name

    /**
     * associated content
     */
    Set<Value> value

}
