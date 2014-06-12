package moa
/**
 * who submitted this spectra
 */
class Submitter {
    static mapWith = "mongo"

    static constraints = {
        emailAddress(unique: true, blank: false)
        firstName(blank: false)
        lastName(blank: false)
        password(blank: false)
    }

    static hasMany = [spectra: Spectrum]

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

    /**
     * owned spectra
     */
    Set<Spectrum> spectra
}
