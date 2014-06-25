package moa.persistence

import grails.test.spock.IntegrationSpec
import moa.Spectrum

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
class SpectrumControllerSpec extends IntegrationSpec {

    SpectrumController spectrumController = new SpectrumController()
    /**
     * a simple spectrum
     */

    def setup() {
    }

    def cleanup() {
        Spectrum.list().each {
            it.delete()
        }
    }

    void "define a new spectrum"() {

        when:
        spectrumController.request.json = '{"biologicalCompound":{"inchiKey":"OTMSDBZUPAUEDD-UHFFFAOYSA-N","names":"ethan"},"chemicalCompound":{"inchiKey":"OTMSDBZUPAUEDD-UHFFFAOYSA-N","names":"Alkanes, C1-2"},"tags":[{"id":1,"text":"dirty"},{"id":3,"text":"mixed"},{"id":6,"text":"experimental"}],"metadata":[],"submitter":{"emailAddress":"wohlgemuth@ucdavis.edu","firstName":"Gert","id":1,"lastName":"Wohlgemuth","password":"dasdsa"},"spectrum":"123:1234","comments":"this is a comment"}'
        spectrumController.save()

        then:
        Spectrum.list().size() == 1

        Spectrum spec = Spectrum.list().get(0)

        spec.chemicalCompound.inchiKey == "OTMSDBZUPAUEDD-UHFFFAOYSA-N"
        spec.biologicalCompound.inchiKey == "OTMSDBZUPAUEDD-UHFFFAOYSA-N"

        spec.chemicalCompound.names.contains("Alkanes, C1-2")
        spec.biologicalCompound.names.contains("ethan")

        spec.biologicalCompound.names.contains("Alkanes, C1-2")
        spec.chemicalCompound.names.contains("ethan")


    }
}
