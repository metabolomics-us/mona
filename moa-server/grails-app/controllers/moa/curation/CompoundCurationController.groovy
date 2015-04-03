package moa.curation
import moa.Compound
import moa.server.CompoundCurationJob
import moa.server.curation.CompoundCurationService

class CompoundCurationController {

    def sessionFactory


    static responseFormats = ['json']

    CompoundCurationService compoundCurationService

    /**
     * validates the spectra for the given id
     * @param id
     */
    def curate() {

        long id = params.id as long
        boolean result = compoundCurationService.validate(id)

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

        CompoundCurationJob.triggerNow([all:true])

        render (text: "started curration of all compounds!")
    }
}
