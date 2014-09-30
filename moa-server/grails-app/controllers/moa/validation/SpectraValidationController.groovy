package moa.validation

import moa.Spectrum
import moa.server.SpectraValidationJob
import moa.server.validation.SpectraValidationService

class SpectraValidationController {

    def sessionFactory

    static responseFormats = ['json']

    SpectraValidationService spectraValidationService

    /**
     * validates the spectra for the given id
     * @param id
     */
    def validateSpectra() {

        long id = params.id as long
        boolean result = spectraValidationService.validateSpectra(id)

        if (!result) {
            render(status: 503, text: "validation of ${id} failed!")
        } else {
            render( text: "validation of ${id} succesful!")
        }
    }

    /**
     * validate all spectrums
     * @return
     */
    def validateAll(){

        def ids = Spectrum.findAll()*.id

        ids.each {long id ->
            SpectraValidationJob.triggerNow([spectraId:id])
        }
    }
}
