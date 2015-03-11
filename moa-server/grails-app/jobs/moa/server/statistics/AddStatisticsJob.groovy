package moa.server.statistics

import moa.Statistics
import moa.server.curation.SpectraCurationService

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 3/10/15
 * Time: 4:30 PM
 */
class AddStatisticsJob {

    def concurrent = true

/**
 * needs to be defined
 */
    static triggers = {}

    def group = "statistics"

    def description = "adds a new statitics item to the system"

    def execute(context) {
        Map data = context.mergedJobDataMap

        if (data != null && data.object != null) {

            Statistics statistics = data.object

            if(!statistics.validate())    {
                log.error(statistics.errors)
            }
            statistics.save()
        } else {
            log.info("\t=>\tno data was provided")
        }
    }
}
