package moa.server.curation

import curation.CurationObject
import curation.CurationRule
import curation.CurationWorkflow
import grails.plugin.cache.CacheEvict
import grails.transaction.Transactional
import grails.util.Holders
import moa.News
import moa.Spectrum
import moa.server.NewsService
import moa.server.WebhookService
import moa.server.metadata.MetaDataPersistenceService
import moa.server.query.SpectraQueryService
import moa.server.statistics.StatisticsService
import util.FireJobs

@Transactional
class SpectraCurationService {

    SpectraQueryService spectraQueryService

    CurationWorkflow spectraCurationWorkflow


    MetaDataPersistenceService metaDataPersistenceService

    StatisticsService statisticsService

    NewsService newsService

    WebhookService webhookService


    /**
     * runs the curation workflow for the given spectra
     * @param id
     * @return
     */
    boolean validateSpectra(long id) {
        return validateSpectrumByBean(id, null);
    }

    /**
     * runs a curation workflow with the given bean for the given spectrum
     * if no bean is provided, run the full spectrum curation workflow
     * @param id
     * @param bean
     * @return
     */
    boolean validateSpectrumByBean(long id, String bean) {
        long begin = System.currentTimeMillis()
        Date startTime = new Date()

        Spectrum spectrum = spectraQueryService.query(id)
        boolean result;

        if (spectrum) {
            // If no bean is specified, run the full workflow
            if (bean == null) {
                log.info("starting curation for: ${spectraCurationWorkflow}")

                result = spectraCurationWorkflow.runWorkflow(new CurationObject(spectrum))
            } else {
                CurationRule rule = Holders.getApplicationContext().getBean(data.arguments.bean as String)

                log.info("running rule: ${rule.description}")

                CurationWorkflow workflow = new CurationWorkflow();
                workflow.getRules().add(rule)
                result = workflow.runWorkflow(new CurationObject(Spectrum.get(data.spectraId as long)))
            }

            long end = System.currentTimeMillis()
            long needed = (end - begin)

            spectrum = Spectrum.get(spectrum.id)

            metaDataPersistenceService.generateMetaDataObject(spectrum, [name: "validation date", value: new Date().format("dd-MMM-yyyy"), category: "computed", computed: true])
            metaDataPersistenceService.generateMetaDataObject(spectrum, [name: "validation time", value: needed, unit: "ms", category: "computed", computed: true])

            statisticsService.acquire(needed, "${id}", "spectra validation time", "validation")

            spectrum.save()

            String message = "a spectrum was just validated for "

            if(spectrum.chemicalCompound.names != null && spectrum.chemicalCompound.names.size() > 0){
                message += spectrum.chemicalCompound.names[0].name
            } else{
                message += spectrum.chemicalCompound.inchiKey
            }

            newsService.createNews("spectrum validated: ${spectrum.id}", message,
                    "/spectra/display/${spectrum.id}", 60, News.NOTIFICATION, "spectra")

            webhookService.sendWebhookSpectrumUpdates(startTime)

            return result
        } else {
            return false;
        }
    }
}
