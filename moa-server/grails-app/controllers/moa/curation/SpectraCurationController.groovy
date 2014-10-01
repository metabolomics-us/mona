package moa.curation

import moa.Spectrum
import moa.server.SpectraValidationJob
import moa.server.curation.SpectraCurationService

class SpectraCurationController {

    def sessionFactory

    static responseFormats = ['json']

    SpectraCurationService spectraCurationService

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
}
