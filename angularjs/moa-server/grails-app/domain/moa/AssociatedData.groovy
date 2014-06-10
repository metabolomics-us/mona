package moa

import grails.rest.Resource

@Resource(uri = "/rest/metadata", formats = ['json'])
class AssociatedData {

    static constraints = {
    }

    /**
     * it belongs to a parent
     */
    static belongsTo = [parent: AssociatedData]

    /**
     * it can have many spectra or associated data
     */
    static hasMany = [ children: AssociatedData]

    /**
     * identifier of this data object
     */
    String identifier

    /**
     * possible parent in case there is a hierarchy of metadata
     */
    AssociatedData parent

    /**
     * possible list of children in case there is a hierarchy
     */
    Set<AssociatedData> children

    /**
     * associated spectra
     */
    //Set<Spectra> associatedSpectra
}
