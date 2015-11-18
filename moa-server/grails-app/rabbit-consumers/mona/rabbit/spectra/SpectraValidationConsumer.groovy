package mona.rabbit.spectra

import com.budjb.rabbitmq.consumer.MessageContext
import com.budjb.rabbitmq.publisher.RabbitMessagePublisher
import curation.CurationObject
import curation.CurationRule
import curation.CurationWorkflow
import grails.converters.JSON
import grails.util.Holders
import moa.Spectrum
import moa.server.curation.SpectraCurationService
import moa.server.query.SpectraQueryService
import moa.server.scoring.ScoringService
import org.apache.log4j.Logger
import org.springframework.context.ApplicationContext
import util.FireJobs

class SpectraValidationConsumer {

    boolean score = true

    SpectraCurationService spectraCurationService

    SpectraQueryService spectraQueryService

    ScoringService scoringService

    Logger log = Logger.getLogger(getClass())

    RabbitMessagePublisher rabbitMessagePublisher

    static rabbitConfig = [
            queue: "mona.validate.spectra",
            consumers: Runtime.getRuntime().availableProcessors(),
            prefetchCount: 10
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
            if (data.containsKey('spectraId')) {
                long begin = System.currentTimeMillis()

                if (data.arguments.bean != null) {
                    CurationRule rule = Holders.getApplicationContext().getBean(data.arguments.bean as String)

                    log.info("running rule: ${rule.description}")

                    CurationWorkflow workflow = new CurationWorkflow();
                    workflow.getRules().add(rule)
                    workflow.runWorkflow(new CurationObject(Spectrum.get(data.spectraId as long)))
                } else {
                    boolean result = spectraCurationService.validateSpectra(data.spectraId as long)

                    long end = System.currentTimeMillis()

                    long needed = (end - begin)

                    log.debug("validated spectra with id: ${data.spectraId}, which took ${needed / 1000}, success: ${result} ")


                    if (score) {
                        scoringService.score(data.spectraId as long)
                    }
                }
            } else if (data.all) {
                //get all id's in the system
                def ids = spectraQueryService.queryForIds([:]);

                log.debug("found: ${ids.size()} spectra to validate...")

                rabbitMessagePublisher.withChannel { channel ->
                    ids.each { def id ->
                        send {
                            routingKey = "mona.validate.spectra"
                            body = [spectraId: id.id, "arguments": data.arguments]
                            priority = 5
                        }
                    }
                }
            } else if (data.query) {
                def ids = spectraQueryService.queryForIds(JSON.parse(data.query))

                rabbitMessagePublisher.withChannel { channel ->
                    ids.each { def id ->
                        send {
                            routingKey = "mona.validate.spectra"
                            body = [spectraId: id.id, "arguments": data.arguments]
                            priority = 5
                        }
                    }
                }
            } else {
                log.info("\t=>\tno spectraId was provided!")
            }
        } else {
            log.info("\t=>\tno data was provided")
        }
    }
}
