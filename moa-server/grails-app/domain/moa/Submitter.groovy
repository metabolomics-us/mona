package moa

import grails.rest.Resource
import moa.auth.Role
import moa.auth.SubmitterRole

/**
 * who submitted this spectra
 */
@Resource(formats=['json'])
class Submitter {

    Date dateCreated
    Date lastUpdated

    transient springSecurityService

    static transients = ['springSecurityService']

    static constraints = {
        emailAddress unique: true, blank: false
        firstName blank: true
        lastName blank: true
        password blank: false
        spectra nullable: true
        institution nullable: true, blank: true
    }

    static hasMany = [spectra: Spectrum]

    static mapping = {
        password column: '`password`'
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
     * institution of the submitter
     */
    String institution

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

    /**
     * state of the submitter account
     */
    boolean accountEnabled = true
    boolean accountLocked
    boolean accountExpired
    boolean passwordExpired


    Set<Role> getAuthorities() {
        SubmitterRole.findAllBySubmitter(this).collect {
            it.role
        }
    }

    def beforeInsert() {
        encodePassword()
    }

    def beforeUpdate() {
        if (isDirty('password')) {
            encodePassword()
        }
    }

    protected void encodePassword() {
        password = springSecurityService?.passwordEncoder ? springSecurityService.encodePassword(password) : password
    }
}
