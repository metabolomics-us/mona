package moa.server

import moa.Spectrum

/**
 * used to upload a spectra in the background
 */
class SpectraUploadJob {

    /**
     * needs to be defined
     */
    static triggers = {}

    def group = "upload"

    def description = "uploads spectra data in the background of the server"

    SpectraPersistenceService spectraPersistenceService

    def execute(context) {

        Map data = context.mergedJobDataMap

        if (data.containsKey('spectra')) {
            Spectrum result = spectraPersistenceService.create(data.spectra)
            result.save(flush: true)
        }
    }
}
