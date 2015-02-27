package moa.curation

import grails.converters.JSON
import moa.Spectrum
import moa.server.SpectraValidationJob
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

        long id = params.id as long
        boolean result = spectraCurationService.validateSpectra(id)

        if (!result) {
            render(status: 503, text: "curation of ${id} failed!")
        } else {
            render( text: "curation of ${id} succesful!")
        }
    }

    /**
     * validate all spectrums
     * @return
     */
    def curateAll(){

        def ids = Spectrum.findAll()*.id

        ids.each {long id ->
            SpectraValidationJob.triggerNow([spectraId:id])
        }
    }

    /**
     * curates spectra found by the given query the format is the same as in the query service
     */
    def curateByQuery(){

        def query = request.getJSON()

        def spectra = spectraQueryService.query(query,params)

        spectra.each { Spectrum s ->
            SpectraValidationJob.triggerNow([spectraId:s.id])
        }

        render ([message:"validating ${spectra.size()} spectra now"] as JSON )
    }
}
