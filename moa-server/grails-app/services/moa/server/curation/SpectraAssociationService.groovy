package moa.server.curation

import curation.CurationObject
import curation.CurationWorkflow
import grails.transaction.Transactional
import moa.Spectrum
import moa.server.query.SpectraQueryService

/**
 * simple service to reassociate spectra, with their actual users
 */
@Transactional
class SpectraAssociationService {

    CurationWorkflow spectraAssociationWorkflow

    SpectraQueryService spectraQueryService

    /**
     * tries to associate the given spectra with this workflow
     * @param spectraId
     * @return
     */
    def associate(long spectraId) {

        if(Spectrum.get(spectraId)) {
            spectraAssociationWorkflow.runWorkflow(new CurationObject(Spectrum.get(spectraId)))

        }
        else{
            log.warn("spectra id: ${spectraId} was not found!")
        }

    }

    /**
     * tries to associate all spectra id
     * @return
     */
    def associate(){

        spectraQueryService.queryForIds([:]).each {
            associate(it as long)
        }
    }
}
