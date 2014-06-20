package moa.persistence

import grails.test.spock.IntegrationSpec
import moa.Submitter
import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.web.json.JSONArray

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
class SubmitterControllerSpec extends IntegrationSpec {
	def log = LogFactory.getLog(this.class)
	SubmitterController controller

	def setup() {
		controller = new SubmitterController()
	}

	def cleanup() {
		controller = null
		Submitter.deleteAll(Submitter.list())
	}

	void "list should return 2 items"() {
		given:
		final int precount = Submitter.list().size()
		new Submitter(firstName: "Gert", lastName: "Wohlgemuth", emailAddress: "wohlgemuth@ucdavis.edu", password: "dasdsa", spectra: null).save()
		new Submitter(firstName: "Diego", lastName: "Pedrosa", emailAddress: "linuxmant@gmail.com", password: "dsadasd", spectra: null).save()

		when:
		controller.index()

		then:
		// before WE added items the table was empty
		precount == 0
		// check the response status
		controller.response.status == 200
		// right way -- this is testing the content of the response.json object created by the controller
		((JSONArray)controller.response.json).size() == 2
		// indirect way -- this is testing the domain class not the controller action
		Submitter.list().size() == 2
	}

}
