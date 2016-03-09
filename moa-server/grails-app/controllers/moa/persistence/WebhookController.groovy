package moa.persistence

import grails.converters.JSON
import grails.rest.RestfulController
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import moa.Webhook
import moa.server.WebhookService

/**
 * Created by wohlgemuth on 10/2/15.
 */
class WebhookController extends RestfulController {

    static responseFormats = ['json']

    WebhookService webhookService


    public WebhookController() {
        super(Webhook)
    }

    @Override
    protected Map getParametersToBind() {
        if (request.JSON) {
            params.putAll(request.JSON)
        }

        params
    }

    def gnpsUpdate() {
        def id = params.id
        def event = params.event

        log.info("Received GNPS update (event = $event) for $id")

        def url = "http://gnps.ucsd.edu"

        def http = new HTTPBuilder(url)
        http.request(Method.GET, ContentType.JSON) { req ->
            uri.path = "/ProteoSAFe/SpectrumCommentServlet?SpectrumID=$id"
            requestContentType = ContentType.JSON

            response.success = { resp, json ->
                log.info("Success! ${resp.status}")
                log.info(json)
            }
        }

        render([message: "update submitted"] as JSON)
    }
}

