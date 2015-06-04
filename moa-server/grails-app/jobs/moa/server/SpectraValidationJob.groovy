package moa.server

import curation.rules.spectra.RemoveIdenticalSpectraRule
import grails.converters.JSON
import moa.Spectrum
import moa.server.curation.SpectraCurationService
import moa.server.query.SpectraQueryService
import moa.server.scoring.ScoringService
import util.FireJobs

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 9/30/14
 * Time: 1:30 PM
 */
class SpectraValidationJob {
    def concurrent = true

    boolean score = true
    /**
     * needs to be defined
     */
    static triggers = {}

    def group = "validation-spectra"

    def description = "validates spectra data in the background of the server"

    SpectraCurationService spectraCurationService

    SpectraQueryService spectraQueryService

    ScoringService scoringService
    def execute(context) {
        Map data = context.mergedJobDataMap

        if (data != null) {
            if (data.containsKey('spectraId')) {

                long begin = System.currentTimeMillis()

                boolean result = spectraCurationService.validateSpectra(data.spectraId as long)

                long end = System.currentTimeMillis()

                long needed = (end - begin)

                log.debug("validated spectra with id: ${data.spectraId}, which took ${needed / 1000}, success: ${result} ")


                if(score){
                    scoringService.score(data.spectraId as long)
                }

            }
            else if (data.all) {
                //get all id's in the system
                def ids = spectraQueryService.queryForIds(
                        [:
                        ]
                );

                log.debug("found: ${ids.size()} spectra to validate...")
                ids.eachParallel { long id ->
                    log.debug("scheduling spectra for curration with id: ${id}")
                    FireJobs.fireSpectraCurationJob([spectraId: id])
                }

            } else if (data.query) {

                def spectra = spectraQueryService.query(JSON.parse(data.query))

                spectra.each { Spectrum s ->
                    FireJobs.fireSpectraCurationJob([spectraId: s.id])
                }


            } else {
                log.info("\t=>\tno spectraId was provided!")
            }
        } else {
            log.info("\t=>\tno data was provided")
        }
    }
}
