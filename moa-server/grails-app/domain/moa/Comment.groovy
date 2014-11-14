package moa

/**
 * a simple comments for a compound or spectra
 */
class Comment {

    static constraints = {
        comment unique: false
    }

    static mapping = {
        comment sqlType: "text"
    }

    String comment
}
