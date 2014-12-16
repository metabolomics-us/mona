package moa

class Tag implements Serializable{

    static constraints = {
        text blank: false, unique: true, nullable: false
        ruleBased unique: false, nullable: true
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

    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof Tag)) return false

        Tag tag = (Tag) o

        if (text != tag.text) return false

        return true
    }

    int hashCode() {
        return (text != null ? text.hashCode() : 0)
    }

}
