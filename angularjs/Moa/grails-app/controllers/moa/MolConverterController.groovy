package moa

import grails.converters.JSON

class MolConverterController {
    static responseFormats = ['json']

    /**
     * converts a mol file to an inchi code
     */
    def molToInChICode() {

    }

    /**
     * converts a mol file to an inchi key
     */
    def moltoinchi() {
        log.warn("we are running in DUMMY mode, and always return the same inchi")
        log.warn("params: ${params}" )
        log.warn("json: ${request.JSON}")

        ["key":"QNAYBMKLOCPYGJ-UWTATZPHSA-N"]
    }
}
