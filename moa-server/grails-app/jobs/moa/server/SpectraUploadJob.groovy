package moa.server

import exception.ValidationException
import grails.converters.JSON
import moa.Spectrum
import org.codehaus.groovy.grails.web.json.JSONObject
/**
 * used to upload a spectra in the background
 */
class SpectraUploadJob {

    def concurrent = false

    /**
     * needs to be defined
     */
    static triggers = {}

    def group = "upload"

    def description = "uploads spectra data in the background of the server"

    SpectraPersistenceService spectraPersistenceService

    def execute(context) {
        Map data = context.mergedJobDataMap

        if (data != null) {
            if (data.containsKey('spectra')) {
                long begin = System.currentTimeMillis()

                try {

                    def json = null
                    if(data.spectra instanceof JSONObject){
                        json = data.spectra
                    }
                    else{
                        json = JSON.parse(data.spectra);
                    }

                    Spectrum result = spectraPersistenceService.create(json)
                    //result.save(flush: true)

                    long end = System.currentTimeMillis()

                    long needed = end - begin
                    log.debug( "stored spectra with id: ${result.id}, InChI: ${result.chemicalCompound.inchiKey}, which took ${needed / 1000}" )


                    SpectraValidationJob.triggerNow([spectraId:result.id])

                }
                catch (ValidationException e){
                    log.debug("validation error found: ${e.getMessage()} ignoring this ojbect and skipping it from the upload")
                    log.debug(JSON.parse(data.spectra) as JSON,e)
                }
                catch (Exception e){
                    log.warn("resubmitting failed job",e)
                    log.debug(JSON.parse(data.spectra) as JSON,e)

                    SpectraUploadJob.triggerNow([spectra: data.spectra])

                }
            } else {
                log.info("\t=>\tno spectra was provided!")
            }
        } else {
            log.info("\t=>\tno data was provided")
        }
    }
}
