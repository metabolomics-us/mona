package moa.service

import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap

class MolConverterController {
    static responseFormats = ['json']

    def ctsUrl = "http://127.0.0.1:9999/cts"
    def rest

    /**
     * converts a mol file to an inchi key
     */
    def moltoinchi() {

        def object = request.JSON.mol.toString()
        log.info("generating inchi for: ${object}")


        MultiValueMap<String, String> form = new LinkedMultiValueMap<String, String>()

        form.add("mol", object)

        def resp = rest.post("${ctsUrl}/service/moltoinchi") {
            accept "application/json"
            body(form)
        }

        response.status = resp.getStatusCode().value();
        log.info("response: " + resp.json)
        respond(["key": resp.json.inchikey])

    }

    /**
     * generates a molecule for the given inchi key
     * @return
     */
    def inchiKeyToMol() {

        println(request.JSON)
        log.info("requesting names for ${params.ichi}")
        def resp = rest.get("${ctsUrl}/service/inchikeytomol/${request.JSON.inchi}")


        response.status = resp.getStatusCode().value();
        respond(["molecule": resp.json.molecule])
    }
}
