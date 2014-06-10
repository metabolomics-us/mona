package moa

import grails.rest.Resource

@Resource()
class Compound {
    def mongo

    static constraints = {
    }

    /**
     * inchiKey of this compound
     */
    String inchiKey

    /**
     * inchiCode of this compound
     */
    String inchiCode

}
