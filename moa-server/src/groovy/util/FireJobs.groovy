package util

import grails.plugins.quartz.TriggerUtils
import groovy.time.TimeCategory
import moa.server.CompoundCurationJob
import moa.server.SpectraAssociationJob
import moa.server.SpectraUploadJob
import moa.server.SpectraValidationJob

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 5/22/15
 * Time: 9:42 AM
 */
class FireJobs {

    static fireSpectraCurationJob(Map data){
        Date date = new Date()

        Date schedule = null;
        use(TimeCategory) {
            schedule = date + 5.seconds
        }

        SpectraValidationJob.triggerNow(data)
    }

    static fireSpectraUploadJob(Map data){
        Date date = new Date()

        Date schedule = null;
        use(TimeCategory) {
            schedule = date + 5.seconds
        }
        SpectraUploadJob.triggerNow(data)
    }

    static fireSpectraAssociationJob(Map data){
        Date date = new Date()

        Date schedule = null;
        use(TimeCategory) {
            schedule = date + 5.seconds
        }
        SpectraAssociationJob.triggerNow(data)
    }

    static fireCompoundCurationJob(Map data){

        Date date = new Date()

        Date schedule = null;
        use(TimeCategory) {
            schedule = date + 5.seconds
        }
        CompoundCurationJob.triggerNow(data)
    }
}
