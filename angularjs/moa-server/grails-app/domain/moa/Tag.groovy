package moa

class Tag {
    static mapWith = "mongo"

    static constraints = {
<<<<<<< HEAD
        text(blank: false, unique: true)
||||||| merged common ancestors
        text(blank: false)
=======
        text(blank: false, unique: true)
    }

    static mapping = {
        text index: true, indexAttributes: [unique: true]
>>>>>>> origin/master
    }

    /**
     * name of tag
     */
    String text
}
