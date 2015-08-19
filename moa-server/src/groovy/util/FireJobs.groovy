package util

import com.budjb.rabbitmq.publisher.RabbitMessagePublisher
import grails.util.Holders

import groovy.time.TimeCategory
import moa.server.SpectraAssociationJob

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 5/22/15
 * Time: 9:42 AM
 */
class FireJobs {

    static fireSpectraCurationJob(Map data,def params = [:]) {
        data.put("arguments",params)
        Holders.getApplicationContext().getBean(RabbitMessagePublisher.class).send {
            routingKey = "mona.validate.spectra"
            priority = 5
            body = data
        }
    }

    static fireSpectraUploadJob(Map data) {
        Holders.getApplicationContext().getBean(RabbitMessagePublisher.class).send {
            routingKey = "mona.import.spectra"
            body = data
            priority = 8
        }

    }

    static fireSpectraAssociationJob(Map data) {
        Date date = new Date()

        Date schedule = null;
        use(TimeCategory) {
            schedule = date + 5.seconds
        }
        SpectraAssociationJob.triggerNow(data)
    }

    static fireCompoundCurationJob(Map data) {
        Holders.getApplicationContext().getBean(RabbitMessagePublisher.class).send {
            routingKey = "mona.validate.compound"
            body = data
            priority = 6
        }
    }
}
