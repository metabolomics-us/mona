package moa

import grails.rest.Resource

@Resource(uri="/rest/spectra", formats=['json'])
class Spectra {

    /**
     * belongs to this compound
     */
    static belongsTo = [compound:Compound,submitter:Submitter]

    /**
     * we can have a lot of associated data
     */
    static hasMany = [associatedDatas: AssociatedData]

    static constraints = {
    }

    /**
     * our actual massspectra
     */
    Map<Double,Double> massSpectra

    /**
     * the related compound
     */
    Compound compound

    /**
     * related data for this spectra
     */
    Set<AssociatedData> associatedDatas

    /**
     * the person who submitted this particular spectra
     */
    Submitter submitter
}
