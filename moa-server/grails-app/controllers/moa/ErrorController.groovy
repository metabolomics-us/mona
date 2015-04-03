package moa

import grails.converters.JSON

class ErrorController {


    def handle404() {
        response.status = 404
        render([message: "sorry this endpoint was not found!"] as JSON)
    }

    def handle500() {
        response.setContentType "application/json; charset=utf-8"
        response.status = 500

        render([
                code     : 500,
                errorType: "${request.exception.class.getSimpleName()}",
                msgs     : "${((Exception) request.exception).getMessage()}"
        ] as JSON)

    }
}
