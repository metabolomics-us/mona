package moa

import curation.scoring.Scoreable
import moa.scoring.Score

class Name implements Scoreable {

    Date dateCreated
    Date lastUpdated

    static mapping = {
        name sqlType: "text"
        version false
    }

    static constraints = {
        computed nullable: true
        score nullable: true
        source nullable: true
    }

    static belongsTo = [compound: Compound]

    String name

    /**
     * has this name be somehow computed
     */
    Boolean computed

    /**
     * the score of name
     */
    Score score

    String source
    /**
     * set deleted to false as default value
     * @return
     */
    def beforeValidate() {

        if (computed == null) {
            computed = false
        }
    }
}
