package moa.server

import util.StaticProperties
import util.StaticProperties.*

class MaxFilterFilters {

    def filters = {
        addedMaxAttribute(controller:'*', action:'*') {
            before = {

                if(params.max == null){
                    params.max = StaticProperties.MAX_QUERY_RESULTS
                }

                log.info("received parameters: ${params}")
            }
            after = { Map model ->

            }
            afterView = { Exception e ->

            }
        }
    }
}
