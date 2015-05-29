package moa.curation

import moa.server.curation.SpectraCurationService
import util.FireJobs

class SpectraCurationController {

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

        FireJobs.fireSpectraCurationJob([spectraId: id as long])


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

    /**
     * curates spectra found by the given query the format is the same as in the query service
     */
    def curateByQuery() {

        def query = request.getJSON()

        FireJobs.fireSpectraCurationJob([query: query, params: params])

        render(text: "curating all spectra, by query!")

    }
}
