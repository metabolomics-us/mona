package moa.server

import grails.converters.JSON
import moa.Spectrum
import moa.server.curation.SpectraAssociationService
import moa.server.curation.SpectraCurationService
import moa.server.query.SpectraQueryService
import moa.server.scoring.ScoringService
import util.FireJobs

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 6/3/15
 * Time: 11:36 AM
 */
class SpectraAssociationJob {
    def concurrent = true

    /**
     * needs to be defined
     */
    static triggers = {}

    def group = "association-spectra"

    def description = "associates spectra data in the background of the server"

    SpectraAssociationService spectraAssociationService

    SpectraQueryService spectraQueryService

    def execute(context) {
        Map data = context.mergedJobDataMap

        if (data != null) {
            if (data.containsKey('spectraId')) {

                long begin = System.currentTimeMillis()

                try {
                    spectraAssociationService.associate(data.spectraId as long)
                }
                catch (org.springframework.dao.CannotAcquireLockException e){
                    FireJobs.fireSpectraAssociationJob([spectraId:data.spectraId])
                }
                long end = System.currentTimeMillis()

                long needed = (end - begin)

                log.debug("associated spectra with id: ${data.spectraId}, which took ${needed / 1000}")

            } else if (data.all) {
                //get all id's in the system
                def ids = spectraQueryService.queryForIds(
                        [:
                        ]
                );

                log.debug("found: ${ids.size()} spectra to associate...")
                ids.each { def id ->
                    id = id.id
                    log.debug("scheduling spectra for association with id: ${id}")
                    FireJobs.fireSpectraAssociationJob([spectraId: id])
                }

            }
            else if (data.query) {

                def spectra = spectraQueryService.queryForIds(JSON.parse(data.query))

                spectra.each { id ->
                    id = id.id
                    log.debug("scheduling spectra for association with id: ${id}")
                    FireJobs.fireSpectraAssociationJob([spectraId: id])
                }


            }
            else {
                log.info("\t=>\tno spectraId was provided!")
            }
        }

    }
}
