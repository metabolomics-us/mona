package moa
/**
 * who submitted this spectra
 */
class Submitter {
    def mongo

    static constraints = {
        emailAddress(unique: true, blank: false)
        firstName(blank: false)
        lastName(blank: false)
        password(blank: false)
    }

    /**
     * first name of the submitter
     */
    String firstName

    /**
     * last name of the submitter
     */
    String lastName

    /**
     * email address of the submitter
     */
    String emailAddress

    /**
     * our password
     */
    String password
}
