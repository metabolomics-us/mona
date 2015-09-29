package moa.server

import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

class InvokeFilters {

    def filters = {
        all(controller: '*', action: '*') {

            String ip = "unknown"
            try {
                ip = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getRemoteAddr()
            } catch (Exception e) {
                log.debug(e.getMessage(),e)
            }

            long timeBegin = 0
            before = {
                timeBegin = System.currentTimeMillis()

            }
            after = {

                log.debug("time to execute ${controllerName}/${actionName}: ${(System.currentTimeMillis() - timeBegin) / 1000}, ip:${ip}, params: ${params}")

            }
            afterView = {

                log.debug("total execution ${controllerName}/${actionName}: ${(System.currentTimeMillis() - timeBegin) / 1000}, ip:${ip}, params: ${params}")
            }
        }
    }
}
