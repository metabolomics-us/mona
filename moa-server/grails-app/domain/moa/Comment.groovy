package moa

/**
 * a simple comments for a compound or spectra
 */
class Comment {

    Date dateCreated
    Date lastUpdated

    static constraints = {
        comment unique: false
    }

    static mapping = {
        comment sqlType: "text"
        version false
    }

    String comment
}
