package moa

/**
 * Created by sajjan on 2/10/16.
 */
class Library {

    Date dateCreated

    static constraints = {
        library nullable: false
        description nullable: false
        link nullable: true
        tag nullable: false
    }

    static mapping = {
        library sqlType: "text"
        description sqlType: "text"
        version false
    }


    /**
     * name of library
     */
    String library

    /**
     * library description
     */
    String description

    /**
     * link to external library including a placeholder for the spectrum id
     */
    String link

    /**
     * name of the statistics object
     */
    Tag tag
}