package moa.server

import exception.ValidationException
import grails.converters.JSON
import moa.Spectrum
import moa.server.curation.SpectraCurationService
import moa.server.statistics.StatisticsService
import org.codehaus.groovy.grails.web.json.JSONObject
import util.FireJobs

/**
 * used to upload a spectra in the background
 */
class SpectraUploadJob {

    /**
     * do we automatically want to resubmit failed jobs
     */
    def resubmit = true

    /**
     * should this run concurrent over the whole cluster
     */
    def concurrent = true

    /**
     * do we want to automatically validate the spectra after submission
     */
    def validation = true

    /**
     * needs to be defined
     */
    static triggers = {}

    def group = "upload"

    def description = "uploads spectra data in the background of the server"

    SpectraPersistenceService spectraPersistenceService

    SpectraCurationService spectraCurationService

    StatisticsService statisticsService

    def execute(context) {
        Map data = context.mergedJobDataMap

        if (data != null) {
            if (data.containsKey('spectra')) {
                long begin = System.currentTimeMillis()

                try {

                    def json = null
                    if (data.spectra instanceof JSONObject) {
                        json = data.spectra
                    } else {
                        json = JSON.parse(data.spectra);
                    }

                    Spectrum result = spectraPersistenceService.create(json)
                    result.save()

                    long end = System.currentTimeMillis()

                    long needed = end - begin
                    log.debug("stored spectra with id: ${result.id}, InChI: ${result.chemicalCompound.inchiKey}, which took ${needed / 1000}")

                    statisticsService.acquire(needed,"${result.id}","spectra import time","import")

                    //automatic validation
                    if(validation) {
                        try {
                            spectraCurationService.validateSpectra(result.id)
                        }
                        catch (Exception e){
                            log.warn("none fatal exception, but spectra submission was succcessful: ${e.getMessage()}",e)
                        }
                    }
                }
                catch (ValidationException e) {

                    JSON json = JSON.parse(data.spectra) as JSON
                    json.prettyPrint = true

                    log.debug("validation error found: ${e.getMessage()} ignoring this ojbect and skipping it from the upload")
                    log.debug(json, e)
                }
                catch (Exception e) {

                    JSON json = JSON.parse(data.spectra) as JSON
                    json.prettyPrint = true
                    log.debug(json, e)

                    if (resubmit) {
                        log.error("resubmitting failed job to the system", e)

                        SpectraUploadJob.triggerNow([spectra: data.spectra])
                    } else {
                        log.error("upload fatally failed: ${e.getMessage()}", e)
                    }
                }
            } else {
                log.info("\t=>\tno spectra was provided!")
            }
        } else {
            log.info("\t=>\tno data was provided")
        }
    }
}
