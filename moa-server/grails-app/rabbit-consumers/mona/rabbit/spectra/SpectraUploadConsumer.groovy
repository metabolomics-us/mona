package mona.rabbit.spectra

import com.budjb.rabbitmq.consumer.MessageContext
import com.budjb.rabbitmq.publisher.RabbitMessagePublisher
import exception.ValidationException
import grails.converters.JSON
import moa.Spectrum
import moa.server.SpectraPersistenceService
import moa.server.SpectraUploadJob
import moa.server.curation.SpectraCurationService
import moa.server.scoring.ScoringService
import moa.server.statistics.StatisticsService
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.web.json.JSONObject
import util.FireJobs

class SpectraUploadConsumer {

    SpectraPersistenceService spectraPersistenceService

    SpectraCurationService spectraCurationService

    StatisticsService statisticsService

    ScoringService scoringService

    Logger log = Logger.getLogger(getClass())

    RabbitMessagePublisher rabbitMessagePublisher

    def validation = true

    def resubmit = true

    static rabbitConfig = [
            queue    : "mona.import.spectra",
            consumers: 100
    ]

    /**
     * Handle an incoming RabbitMQ message.
     *
     * @param body The converted body of the incoming message.
     * @param context Properties of the incoming message.
     * @return
     */
    def handleMessage(def data, MessageContext context) {
        if (data != null) {
            if (data.containsKey('spectra')) {
                long begin = System.currentTimeMillis()

                try {
                    def json = (data.spectra instanceof JSONObject) ? data.spectra : JSON.parse(data.spectra);

                    Spectrum result = spectraPersistenceService.create(json)
                    result.save(flush: true)

                    long end = System.currentTimeMillis()

                    long needed = end - begin
                    log.debug("stored spectra with id: ${result.id}, InChI: ${result.chemicalCompound.inchiKey}, which took ${needed / 1000}")

                    statisticsService.acquire(needed, "${result.id}", "spectra import time", "import")

                    //automatic scoring, so we have some score at least, even if it's null
                    try {
                        scoringService.score(result)
                    } catch (Exception e) {
                        log.warn("none fatal exception, but spectra submission was succcessful: ${e.getMessage()}", e)
                    }

                    //automatic validation
                    if (validation) {
                        try {
                            FireJobs.fireSpectraCurationJob([spectraId: id.id])
                        } catch (Exception e) {
                            log.warn("none fatal exception, but spectra submission was succcessful: ${e.getMessage()}", e)
                        }
                    }
                } catch (ValidationException e) {
                    JSON json = JSON.parse(data.spectra) as JSON
                    json.prettyPrint = true

                    log.debug("validation error found: ${e.getMessage()} ignoring this ojbect and skipping it from the upload")
                    log.debug(json, e)
                } catch (Exception e) {
                    JSON json = JSON.parse(data.spectra) as JSON
                    json.prettyPrint = true

                    log.debug(json, e)

                    if (resubmit) {
                        if (e instanceof IllegalArgumentException) {
                            log.error("fatal error - no resubmission possible", e)
                        } else {
                            log.error("resubmitting failed job to the system", e)
                            FireJobs.fireSpectraUploadJob(data)
                        }
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
