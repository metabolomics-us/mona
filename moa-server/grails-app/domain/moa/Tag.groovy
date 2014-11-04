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


    int getSpectraCount() {

        int value = 0

        withSession { session ->
            def result = session.createSQLQuery(" select count(*) as c from spectrum_tag a, tag b where a.tag_id = b.id and b.text = ? group by text").setString(0, text).list()

            if(!result.isEmpty()){
                value = result[0]
            }
        }

        return value
    }

    int getCompoundCount() {

        int value = 0

        withSession { session ->
            def result = session.createSQLQuery(" select count(*) as c from compound_tag a, tag b where a.tag_id = b.id and b.text = ? group by text").setString(0, text).list()

            if(!result.isEmpty()){
                value = result[0]
            }
        }

        return value    }
    static transients = ['spectraCount', 'compoundCound']

}
