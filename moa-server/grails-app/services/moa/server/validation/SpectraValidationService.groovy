package moa.server.validation

import grails.plugin.cache.CacheEvict
import grails.transaction.Transactional
import moa.Spectrum
import moa.server.query.SpectraQueryService
import validation.ValidationWorkflow

@Transactional
class SpectraValidationService {

    SpectraQueryService spectraQueryService

    ValidationWorkflow validationWorkflow

    /**
     * runs the validation workflow for the given spectra
     * @param id
     * @return
     */
    @CacheEvict(value='spectrum', allEntries=true)
    boolean validateSpectra(long id) {
        Spectrum spectrum = spectraQueryService.query(id)

        return validationWorkflow.runWorkflow(spectrum)
    }
}
