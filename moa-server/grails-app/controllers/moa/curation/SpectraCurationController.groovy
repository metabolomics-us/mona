package moa.curation

import moa.server.curation.SpectraCurationService
import util.FireJobs

class SpectraCurationController {

    static allowedMethods = [
            curate:"GET",
            curateNow:"GET",
            curateAll:"GET",
            curateAllByQuery: ["POST","GET"],
            associate: "GET",
            associateAll: "GET"
    ]

    static responseFormats = ['json']

    SpectraCurationService spectraCurationService

    /**
     * validates the spectra for the given id
     * @param id
     */
    def curate() {

        def id = params.id

        FireJobs.fireSpectraCurationJob([spectraId: id as long])

        render(text: "scheduling curation of ${id} succesful!")
    }

    def curateNow() {

        def id = params.id

        render (success: spectraCurationService.validateSpectra(id as long))

    }


    /**
     * validate all spectrums
     * @return
     */
    def curateAll() {
        FireJobs.fireSpectraCurationJob([all: true])
        render(text: "curating all spectra!")
    }

    def associateAll(){
        FireJobs.fireSpectraAssociationJob([all: true])

        render(text: "associating all spectra!")
    }

    def associate(){
        def id = params.id

        FireJobs.fireSpectraAssociationJob([spectraId: id as long])

        render(text: "scheduling association of ${id} succesful!")
    }
    /**
     * curates spectra found by the given query the format is the same as in the query service
     */
    def curateAllByQuery() {

        def query = request.getJSON().toString()

        log.info("received query: ${query}")

        FireJobs.fireSpectraCurationJob([query: query])

        render(text: "curating all spectra, by query!")

    }
}
