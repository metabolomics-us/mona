package moa

import grails.rest.Resource

/**
 * who submitted this spectra
 */
@Resource()
class Submitter {

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
