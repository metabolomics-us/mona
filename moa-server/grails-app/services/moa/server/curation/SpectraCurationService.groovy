package moa.server.curation

import curation.CurrationObject
import grails.plugin.cache.CacheEvict
import grails.transaction.Transactional
import moa.Spectrum
import moa.server.query.SpectraQueryService
import curation.CurationWorkflow

@Transactional
class SpectraCurationService {

    SpectraQueryService spectraQueryService

    CurationWorkflow curationWorkflow

    /**
     * runs the curation workflow for the given spectra
     * @param id
     * @return
     */
    @CacheEvict(value='spectrum', allEntries=true)
    boolean validateSpectra(long id) {
        Spectrum spectrum = spectraQueryService.query(id)

        return curationWorkflow.runWorkflow(new CurrationObject(spectrum))
    }
}
