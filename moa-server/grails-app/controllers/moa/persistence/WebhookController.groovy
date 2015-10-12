package moa.persistence

import grails.rest.RestfulController
import moa.Webhook

/**
 * Created by wohlgemuth on 10/2/15.
 */
class WebhookController extends RestfulController{


    static responseFormats = ['json']

    public WebhookController(){
        super(Webhook)
    }

    @Override
    protected Map getParametersToBind() {
        if (request.JSON) {
            params.putAll(request.JSON)
        }

        params
    }
}

