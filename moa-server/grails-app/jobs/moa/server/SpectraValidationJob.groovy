package moa.server
import moa.server.curation.SpectraCurationService
/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 9/30/14
 * Time: 1:30 PM
 */
class SpectraValidationJob {
    def concurrent = false

    /**
     * needs to be defined
     */
    static triggers = {}

    def group = "curation"

    def description = "uploads spectra data in the background of the server"

    SpectraCurationService spectraCurationService

    def execute(context) {
        Map data = context.mergedJobDataMap

        if (data != null) {
            if (data.containsKey('spectraId')) {

                long begin = System.currentTimeMillis()

                boolean result = spectraCurationService.validateSpectra(data.spectraId as long)

                long end = System.currentTimeMillis()

                long needed = (end - begin)

                log.debug( "validated spectra with id: ${data.spectraId}, which took ${needed / 1000}, success: ${result} ")

            } else {
                log.info("\t=>\tno spectraId was provided!")
            }
        } else {
            log.info("\t=>\tno data was provided")
        }
    }
}
