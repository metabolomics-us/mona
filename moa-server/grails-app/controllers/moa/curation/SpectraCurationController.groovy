package moa.curation

import grails.converters.JSON
import moa.Spectrum
import moa.server.SpectraValidationJob
import moa.server.SpectraValidationSchedulingJob
import moa.server.curation.SpectraCurationService
import moa.server.query.SpectraQueryService

class SpectraCurationController {

    static responseFormats = ['json']

    SpectraCurationService spectraCurationService

    /**
     * validates the spectra for the given id
     * @param id
     */
    def curate() {

        def id = params.id

        SpectraValidationJob.triggerNow([spectraId: id as long, priority: 3])

        render(text: "scheduling curation of ${id} succesful!")
    }

    def curateNow() {

        def id = params.id

        SpectraValidationJob.triggerNow([spectraId: id as long, priority: 3])


        render (success: spectraCurationService.validateSpectra(id as long))

    }


    /**
     * validate all spectrums
     * @return
     */
    def curateAll() {
        SpectraValidationSchedulingJob.triggerNow([all: true, priority: 3])
        render(text: "curating all spectra!")

    }

    /**
     * curates spectra found by the given query the format is the same as in the query service
     */
    def curateByQuery() {

        def query = request.getJSON()

        SpectraValidationSchedulingJob.triggerNow([query: query, params: params, priority: 3])

        render(text: "curating all spectra, by query!")

    }
}
