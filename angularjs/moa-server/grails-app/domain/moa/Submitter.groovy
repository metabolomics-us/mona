package moa

import grails.rest.Resource

/**
 * who submitted this spectra
 */
@Resource(formats=['json'])
class Submitter {

    static constraints = {
	    emailAddress unique: true, blank: false
	    firstName blank: false
	    lastName blank: false
	    password blank: false
	    spectra nullable: true
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
