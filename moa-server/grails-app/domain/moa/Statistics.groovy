package moa

/**
 * simples object to track statistics in the system
 */
class Statistics {

    static constraints = {
    }

    /**
     * which category is this object under
     */
    String category

    /**
     * description
     */
    String description

    /**
     * name of the statistics object
     */
    String title

    /**
     * numeric value
     */
    Double value

    /**
     * when did this even occure
     */
    Date dateCreated

    static mapping = {
        description sqlType: "text"
        version false
    }
}
