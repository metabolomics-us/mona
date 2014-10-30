package moa.server.curation

import curation.CurationObject
import curation.CurationWorkflow
import grails.plugin.cache.CacheEvict
import grails.transaction.Transactional
import moa.Spectrum
import moa.server.metadata.MetaDataPersistenceService
import moa.server.query.SpectraQueryService

@Transactional
class SpectraCurationService {

    SpectraQueryService spectraQueryService

    CurationWorkflow spectraCurationWorkflow

    MetaDataPersistenceService metaDataPersistenceService

    /**
     * runs the curation workflow for the given spectra
     * @param id
     * @return
     */
    @CacheEvict(value = 'spectrum', allEntries = true)
    boolean validateSpectra(long id) {
        long begin = System.currentTimeMillis()

        Spectrum spectrum = spectraQueryService.query(id)

        boolean result = spectraCurationWorkflow.runWorkflow(new CurationObject(spectrum))

        long end = System.currentTimeMillis()

        long needed = (end - begin)

        spectrum = Spectrum.get(spectrum.id)
        metaDataPersistenceService.generateMetaDataObject(spectrum, [name: "validation date", value: new Date().format("dd-MMM-yyyy"), category: "computed", computed: true])
        metaDataPersistenceService.generateMetaDataObject(spectrum, [name: "validation time", value: needed, unit: "ms", category: "computed", computed: true])

        spectrum.save(flush: true)

        return result

    }
}
