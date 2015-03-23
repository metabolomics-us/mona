package moa.curation

import grails.converters.JSON
import moa.Spectrum
import moa.server.SpectraValidationJob
import moa.server.SpectraValidationSchedulingJob
import moa.server.curation.SpectraCurationService
import moa.server.query.SpectraQueryService

class SpectraCurationController {

    def sessionFactory

    static responseFormats = ['json']

    SpectraCurationService spectraCurationService

    SpectraQueryService spectraQueryService


    /**
     * validates the spectra for the given id
     * @param id
     */
    def curate() {

        def id = params.id

        //long id = params.id as long
        boolean result = spectraCurationService.validateSpectra(id as long)

        if (!result) {
            render(status: 503, text: "curation of ${id} failed!")
        } else {
            render(text: "curation of ${id} succesful!")
        }
    }

    /**
     * validate all spectrums
     * @return
     */
    def curateAll() {
        SpectraValidationSchedulingJob.triggerNow([all:true])
        render(text: "curating all spectra!")

    }

    /**
     * curates spectra found by the given query the format is the same as in the query service
     */
    def curateByQuery() {

        def query = request.getJSON()

        SpectraValidationSchedulingJob.triggerNow([query:query,params:params])

        render(text: "curating all spectra, by query!")

    }
}
