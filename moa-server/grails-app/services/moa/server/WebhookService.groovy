package moa.server

import event.MonaEvent
import grails.transaction.Transactional
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import moa.Submitter
import moa.Webhook
import moa.server.query.SpectraQueryService

@Transactional
class WebhookService {
    SpectraQueryService spectraQueryService

    def sendWebhookSpectrumUpdates(Date startTime) {
        def ids = spectraQueryService.findIdsBylastUpdated(startTime)
        def webhooks = Webhook.findAllByMonaEvent(MonaEvent.UPDATED)

        ids.each { id ->
            webhooks.each { webhook ->
                postData(webhook.targetUrl, id, startTime)
            }
        }
    }

    def postData(String url, long id, Date timestamp) {
        log.info("Sending webook request to $url")

        def http = new HTTPBuilder(url)
        http.request(Method.POST, ContentType.JSON) { req ->
            body = ['spectrum_id': id, 'update_time': timestamp.toTimestamp().toString()]
        }
    }
}
