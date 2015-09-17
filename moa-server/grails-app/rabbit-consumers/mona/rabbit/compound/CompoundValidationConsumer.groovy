package mona.rabbit.compound

import com.budjb.rabbitmq.consumer.MessageContext
import com.budjb.rabbitmq.publisher.RabbitMessagePublisher
import moa.Compound
import moa.server.curation.CompoundCurationService
import org.apache.log4j.Logger

class CompoundValidationConsumer {
    Logger log = Logger.getLogger(getClass())

    /**
     * Consumer configuration.
     */
    static rabbitConfig = [
            queue        : "mona.validate.compound",
            prefetchCount: 10,
            consumers    : 100

    ]

    CompoundCurationService compoundCurationService

    RabbitMessagePublisher rabbitMessagePublisher

    /**
     * Handle an incoming RabbitMQ message.
     *
     * @param body The converted body of the incoming message.
     * @param context Properties of the incoming message.
     * @return
     */
    def handleMessage(def data, MessageContext context) {
        if (data != null) {
            if (data.all) {

                def ids = Compound.findAll()*.id


                rabbitMessagePublisher.withChannel { channel ->

                    ids.each { def id ->

                        send {
                            routingKey = "mona.validate.compound"
                            body = [compoundId: id]
                            priority = 6
                        }
                    }
                }
            } else {
                if (data.containsKey('compoundId') && data.compoundId != null) {
                    long begin = System.currentTimeMillis()


                    boolean result = compoundCurationService.validate(data.compoundId as long)

                    long end = System.currentTimeMillis()

                    long needed = end - begin
                    log.debug("validated compound with id: ${data.compoundId}, which took ${needed / 1000}, success: ${result} ")


                } else {
                    log.info("\t=>\tno compoundId was provided!")
                }
            }
        } else {
            log.info("\t=>\tno data was provided")
        }
    }
}
