package moa.persistence

class SpectrumController {

    def index() { }


    /**
     * otherwise grails won't populate the json fields
     * @return
     */
    protected Map getParametersToBind() {
        if (request.JSON) {
            params.putAll(
                    request.JSON)
        }

        println "modified params: ${params}"
        params
    }
}
