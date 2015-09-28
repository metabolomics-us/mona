package mona.rabbit.spectra

import com.budjb.rabbitmq.consumer.MessageContext
import grails.converters.JSON
import moa.server.curation.SpectraAssociationService
import moa.server.query.SpectraQueryService
import org.apache.log4j.Logger
import util.FireJobs

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 9/14/15
 * Time: 2:07 PM
 */
class SpectraAssociationConsumer {


    /**
     * logger
     */
    Logger log = Logger.getLogger(getClass())


    static rabbitConfig = [
            queue    : "mona.association.spectra",
            consumers: 100        ,
            prefetchCount: 10

    ]


    SpectraAssociationService spectraAssociationService

    SpectraQueryService spectraQueryService

    def handleMessage(def data, MessageContext context) {

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
