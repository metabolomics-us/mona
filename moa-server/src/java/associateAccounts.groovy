/**
 * defines our workflow for spectra curation tasks
 */


import curation.CurationWorkflow
import curation.association.AssociatesMassBankSpectraWithCorrectSubmitter


beans {

    /**
     * associates a given massbank spectra with an external owner
     */
    associatesMassBankSpectraWithCorrectSubmitter(AssociatesMassBankSpectraWithCorrectSubmitter){ bean ->
        bean.autowire = 'byName'
    }

//define our complete workflow here
    spectraAssociationWorkflow(CurationWorkflow) { bean ->
        bean.autowire = 'byName'

        rules = [
                associatesMassBankSpectraWithCorrectSubmitter
        ]
//define and register our curation
    }
}