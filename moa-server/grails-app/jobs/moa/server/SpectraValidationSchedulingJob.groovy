package moa.server

import moa.Spectrum
import moa.server.curation.SpectraCurationService
import moa.server.query.SpectraQueryService

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 3/23/15
 * Time: 9:23 AM
 */
class SpectraValidationSchedulingJob  {
    def concurrent = true

    /**
     * needs to be defined
     */
    static triggers = {}

    def group = "validation-spectra"

    def description = "schedules validation of spectra data in the background of the server"

    SpectraCurationService spectraCurationService

    SpectraQueryService spectraQueryService

    def execute(context) {
        Map data = context.mergedJobDataMap

        if (data != null) {
            if (data.all) {
                def ids = Spectrum.findAll()*.id

                ids.each { long id ->
                    SpectraValidationJob.triggerNow([spectraId: id])
                }

            } else if (data.query) {

                def spectra = spectraQueryService.query(data.query, data.params)

                spectra.each { Spectrum s ->
                    SpectraValidationJob.triggerNow([spectraId: s.id])
                }


            } else {
                log.info("\t=>\tno spectraId was provided!")
            }
        } else {
            log.info("\t=>\tno data was provided")
        }
    }
}
