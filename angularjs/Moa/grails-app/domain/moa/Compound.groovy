package moa

import grails.rest.Resource

@Resource(uri="/rest/compound", formats=['json'])
class Compound {

    static constraints = {
    }

    /**
     * one compound can have many spectra associated to it
     */
    static hasMany = [associatedSpectra:Spectra]


    /**
     * inchiKey of this compound
     */
    String inchiKey

    /**
     * inchiCode of this compound
     */
    String inchiCode

    /**
     * all known associated spectra
     */
    List<Spectra> associatedSpectra
}
