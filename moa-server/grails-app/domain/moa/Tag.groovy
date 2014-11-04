package moa

class Tag {

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

    def afterLoad() {

        log.debug("populating caclulated values for tag: ${text}")

        spectraCount = tagCachingService.computeSpectraCount(this.text)
        compoundCount = tagCachingService.computeCompoundCount(this.text)
    }

    int spectraCount = 0
    int compoundCount = 0
    static transients = ['spectraCount', 'compoundCount']

    def tagCachingService

}
