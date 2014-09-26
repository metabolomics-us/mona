package moa.server

class ExecutionTimeFilters {

    def filters = {
        measureExecutionTime(controller:'*', action:'*') {

            before = {
                request["MEASURE_TIME"] = System.currentTimeMillis();
            }
            after = { Map model ->
                long time = request["MEASURE_TIME"] as long

                log.info("controller execution time: ${System.currentTimeMillis() - time} ms")
            }
            afterView = { Exception e ->
                long time = request["MEASURE_TIME"] as long

                log.info("total execution time: ${System.currentTimeMillis() - time} ms")
            }
        }
    }
}
