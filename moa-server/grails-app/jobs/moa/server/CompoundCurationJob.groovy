package moa.server
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

    def group = "curation"

    def description = "curates compound data in the background of the server"

    CompoundCurationService compoundCurationService

    def execute(context) {
        Map data = context.mergedJobDataMap

        if (data != null) {
            if (data.containsKey('compoundId')) {
                long begin = System.currentTimeMillis()


                boolean result = compoundCurationService.validate(data.compoundId as long)

                long end = System.currentTimeMillis()

                long needed = end - begin
                def message = "validated compound succesffuly:${result} with id: ${data.compoundId}, which took ${needed/1000}s"
                log.info("\t=>\t${message}")

            } else {
                log.info("\t=>\tno compoundId was provided!")
            }
        } else {
            log.info("\t=>\tno data was provided")
        }
    }
}
