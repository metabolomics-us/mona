package mona.rabbit.spectra

import com.budjb.rabbitmq.consumer.MessageContext
import grails.converters.JSON
import moa.SpectrumQueryDownload
import moa.server.query.SpectraQueryExportService
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.web.json.JSONObject
import util.FireJobs;

/**
 * Created by sajjan on 8/31/15.
 */


class SpectraQueryExportConsumer {
    /**
     * service to export spectra queries
     */
    SpectraQueryExportService spectraQueryExportService

    /**
     * logger
     */
    Logger log = Logger.getLogger(getClass())


    /**
     * do we automatically want to resubmit failed jobs
     */
    def resubmit = true


    static rabbitConfig = [
            queue: "mona.export.spectra",
            prefetchCount: 1
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
            if (data.containsKey('query')) {

                if (data.containsKey('emailAddress') || data.containsKey('label')) {
                    long begin = System.currentTimeMillis()

                    // Use a datetime string as an identifier for this job
                    String startTime = data.containsKey('startTime') ? data.startTime : new Date().format('yyyyMMddHHmmssSSS');
                    String format = data.containsKey('query') ? data.query : null

                    try {
                        if(data.containsKey('emailAddress')) {
                            spectraQueryExportService.exportQueryByEmailAddress(data.query, data.emailAddress, startTime, format)
                        } else {
                            spectraQueryExportService.exportQueryByLabel(data.query, data.label, format)
                        }

                        long end = System.currentTimeMillis()
                        log.debug("exported query, which took ${(end - begin) / 1000}")
                    } catch (Throwable e) {
                        def json = (data.query instanceof JSONObject) ? data.query : JSON.parse(data.query)
                        json.prettyPrint = true
                        log.debug(json, e)

                        if (resubmit) {
                            if (e instanceof IllegalArgumentException) {
                                log.error("fatal error - no resubmission possible", e)
                            } else {
                                log.error("resubmitting failed job to the system", e)

                                if(data.containsKey('emailAddress')) {
                                    FireJobs.fireSpectraQueryExportJob([query: data.query, emailAddress: data.emailAddress, startTime: startTime])
                                } else {
                                    FireJobs.fireSpectraQueryExportJob([query: data.query, label: data.label])
                                }
                            }
                        } else {
                            log.error("download fatally failed: ${e.getMessage()}", e)
                        }
                    }
                } else {
                    log.info("\t=>\tno label/email address was provided!")
                }
            } else {
                log.info("\t=>\tno query was provided!")
            }
        } else {
            log.info("\t=>\tno data was provided")
        }
    }
}