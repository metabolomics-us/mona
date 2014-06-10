package moa.service

/**
 * Created by wohlgemuth on 6/8/14.
 */
class CTSController {
    static responseFormats = ['json']

    def rest

    /**
     * connects to the cts and returns all the inchi keys
     */
    def getNamesForInChIKey() {

        def result = []

        log.info("requesting names for ${params.ichi}")
        def resp = rest.get("http://cts.fiehnlab.ucdavis.edu/service/convert/InChIKey/Chemical Name/${params.inchi}")

        if (resp.json.result.size() > 0) {

            resp.json.result.get(0).each{
                result.add(name:it)
            }
            respond(result)
        }
        else{
            respond([])
        }
    }

}
