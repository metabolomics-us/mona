package moa.server.curation

import curation.CurationObject
import curation.CurationWorkflow
import grails.plugin.cache.CacheEvict
import grails.transaction.Transactional
import moa.Spectrum
import moa.server.metadata.MetaDataPersistenceService
import moa.server.query.SpectraQueryService
import moa.server.statistics.StatisticsService

@Transactional
class SpectraCurationService {

    SpectraQueryService spectraQueryService

    CurationWorkflow spectraCurationWorkflow

    MetaDataPersistenceService metaDataPersistenceService

    StatisticsService statisticsService

    /**
     * runs the curation workflow for the given spectra
     * @param id
     * @return
     */
    @CacheEvict(value = 'spectrum', allEntries = true)
    boolean validateSpectra(long id) {
        long begin = System.currentTimeMillis()

        Spectrum spectrum = spectraQueryService.query(id)

        if (spectrum) {
            boolean result = spectraCurationWorkflow.runWorkflow(new CurationObject(spectrum))

            long end = System.currentTimeMillis()

            long needed = (end - begin)

            spectrum = Spectrum.get(spectrum.id)
            metaDataPersistenceService.generateMetaDataObject(spectrum, [name: "validation date", value: new Date().format("dd-MMM-yyyy"), category: "computed", computed: true])
            metaDataPersistenceService.generateMetaDataObject(spectrum, [name: "validation time", value: needed, unit: "ms", category: "computed", computed: true])

            statisticsService.acquire(needed,"${id}","spectra validation time","validation")

            spectrum.save()

            return result
        } else {
            return false
        }

    }
}
