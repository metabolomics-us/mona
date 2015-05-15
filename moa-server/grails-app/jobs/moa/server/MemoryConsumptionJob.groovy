package moa.server
/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 12/18/14
 * Time: 1:23 PM
 */
class MemoryConsumptionJob {

    def concurrent = false

    def group = "maintenance"

    def description = "keeps track of memory consumption of the system"
    static triggers = {
        cron name: 'memoryConsumption', cronExpression: '0 */1 * * * ?', priority: 10
    }

    def execute() {
        log.debug("memory usage, free: ${Runtime.getRuntime().freeMemory()} total: ${Runtime.getRuntime().totalMemory()} used: ${Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()}")
    }
}