package moa.server.curation

import curation.CurationObject
import curation.CurationWorkflow
import grails.plugin.cache.CacheEvict
import grails.transaction.Transactional
import moa.News
import moa.Spectrum
import moa.server.NewsService
import moa.server.metadata.MetaDataPersistenceService
import moa.server.query.SpectraQueryService
import moa.server.statistics.StatisticsService

@Transactional
class SpectraCurationService {

    SpectraQueryService spectraQueryService

    CurationWorkflow spectraCurationWorkflow

    MetaDataPersistenceService metaDataPersistenceService

    StatisticsService statisticsService

    NewsService newsService

    /**
     * runs the curation workflow for the given spectra
     * @param id
     * @return
     */
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

            String message = "a spectrum was just validated for "

            if(spectrum.chemicalCompound.names != null && spectrum.chemicalCompound.names.size() > 0){
                message += spectrum.chemicalCompound.names[0].name
            }
            else{
                message += spectrum.chemicalCompound.inchiKey
            }

            newsService.createNews(
                    "spectrum validated: ${spectrum.id}",
                    message,
                    "/spectra/display/${spectrum.id}",
                    60,
                    News.NOTIFICATION,
                    "spectra"
            )

            return result
        } else {
            return false
        }

    }
}
