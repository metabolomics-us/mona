package moa.server

import moa.Compound
import moa.server.curation.CompoundCurationService
/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 10/13/14
 * Time: 11:00 AM
 */
class CompoundCurationJob {
    def concurrent = true

    /**
     * needs to be defined
     */
    static triggers = {}

    def group = "validation-compound"

    def description = "curates compound data in the background of the server"

    CompoundCurationService compoundCurationService

    def execute(context) {
        Map data = context.mergedJobDataMap

        if (data != null) {
            if(data.all){

                def ids = Compound.findAll()*.id

                ids.each {long id ->
                    CompoundCurationJob.triggerNow([compoundId:id])
                }
            }
            else {
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
