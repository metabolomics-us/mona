package moa

class Value {

    static constraints = {
    }

    static mapping = {
        tablePerHierarchy false
        version false

    }

    /**
     * value we return
     * @return
     */
    Serializable getValue() {
        value
    }

    /**
     * value we can set
     * @param o
     */
    void setValue(Serializable o) {

        value = o
    }

    Serializable value

    static transients = ["value"]

    static belongsTo = [metaData : MetaData]
}
