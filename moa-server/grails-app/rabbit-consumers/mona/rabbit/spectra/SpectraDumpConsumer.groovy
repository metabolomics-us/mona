package mona.rabbit.spectra

import com.budjb.rabbitmq.consumer.MessageContext
import moa.Spectrum
import moa.server.query.SpectraQueryExportService
import moa.server.query.SpectraRepositoryService
import org.apache.log4j.Logger

import javax.sql.DataSource

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 9/28/15
 * Time: 1:45 PM
 */
class SpectraDumpConsumer {
    /**
     * service to export spectra queries
     */
    SpectraRepositoryService spectraRepositoryService

    /**
     * logger
     */
    Logger log = Logger.getLogger(getClass())

    def grailsApplication

    static rabbitConfig = [
            queue: "mona.repository.export.spectra",
            prefetchCount: 100
    ]

    def handleMessage(def data, MessageContext context) {

        if(data!= null){
            if(data.id != null){
                spectraRepositoryService.exportToRepository(Spectrum.load(data.id))
                return
            }
        }
        spectraRepositoryService.exportCreatedToRepositoryFromLastNDays(grailsApplication.config.repository.timeframeInDays)
    }
}