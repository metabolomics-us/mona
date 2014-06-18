package moa.persistence

import grails.test.mixin.TestFor
import moa.Spectrum
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(SpectrumController)
class SpectrumControllerSpec extends Specification {

    /**
     * a simple spectrum
     */

    def setup() {
    }

    def cleanup() {
    }

    void "define a new spectrum"() {

        when:
        controller.request.json = '{"biologicalCompound":{"inchiKey":"OTMSDBZUPAUEDD-UHFFFAOYSA-N","names":"ethan"},"chemicalCompound":{"inchiKey":"OTMSDBZUPAUEDD-UHFFFAOYSA-N","names":"Alkanes, C1-2"},"tags":[{"id":1,"text":"dirty"},{"id":3,"text":"mixed"},{"id":6,"text":"experimental"}],"metadata":[],"submitter":{"emailAddress":"wohlgemuth@ucdavis.edu","firstName":"Gert","id":1,"lastName":"Wohlgemuth","password":"dasdsa"},"spectrum":"123:1234","comments":"this is a comment"}'
        controller.save()

        then:
        Spectrum.list().size() == 1

    }
}
