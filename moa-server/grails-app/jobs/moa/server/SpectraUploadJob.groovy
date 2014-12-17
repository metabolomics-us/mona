package moa.server

import grails.converters.JSON
import moa.Spectrum

/**
 * used to upload a spectra in the background
 */
class SpectraUploadJob {

    def concurrent = true

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

                Spectrum result = spectraPersistenceService.create(JSON.parse(data.spectra))
                result.save(flush: true)

                long end = System.currentTimeMillis()

                long needed = end - begin
                def message = "stored spectra with id: ${result.id}, InChI: ${result.chemicalCompound.inchiKey}, which took ${needed/1000}s"
                log.info("\t=>\t${message}")

            } else {
                log.info("\t=>\tno spectra was provided!")
            }
        } else {
            log.info("\t=>\tno data was provided")
        }
    }
}
