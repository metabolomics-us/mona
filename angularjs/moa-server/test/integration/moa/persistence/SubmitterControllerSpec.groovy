package moa.persistence

import grails.test.spock.IntegrationSpec
import moa.Submitter
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.web.json.JSONArray

import static org.springframework.http.HttpStatus.*

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
class SubmitterControllerSpec extends IntegrationSpec {
	Logger logger = Logger.getLogger(this.class)
	SubmitterController controller = new SubmitterController()

	def setup() {
		controller.request.contentType = "text/json"
		controller.response.format = "json"
	}

	def cleanup() {
		Submitter.deleteAll(Submitter.list())
	}

	// test save action
	void "save a submitter"() {
		given:
		int count = Submitter.count
		Submitter t = new Submitter(firstName: 'one', lastName: 'person', emailAddress: 'yo@mail.com', password: 'dsaasd')

		when: 'saving a new sumbitter'
		controller.request.json = t
		controller.save()

		then: 'it should be persisted'
		controller.response.status == CREATED.value
		count == 0
		Submitter.findByEmailAddress('yo@mail.com').firstName == 'one'
		Submitter.findByEmailAddress('yo@mail.com').lastName == 'person'
		Submitter.count() == 1
	}

	// test show action
	// TODO: add a spectra to test showing of full object
	void "show a submitter"() {

		given:
		def tid = new Submitter(firstName: 'diego', lastName: 'pedrosa', emailAddress: 'mymail@hotmail.com', password: 'baba').save(flush: true)

		when: 'showing one submitter'
		controller.params.id = tid.id
		controller.show()

		then: 'the response status should be 200(OK) and there should be 1 item in the json response'
		Submitter.count() == 1
		controller.response.status == OK.value
		((JSONArray) controller.response.json).containsAll([id: '2', firstName: 'diego', lastName: 'pedrosa', emailAddress: 'mymail@hotmail.com', password: 'baba'])
	}

	// test delete action
	void "delete a submitter"() {

		given:
		int tid = new Submitter(firstName: 'looser', lastName: 'guy', emailAddress: 'byebye@byemail.com', password: 'badpass').save(flush: true).id
		int count = Submitter.count()

		when: 'calling the delete action with parameter id'
		controller.params.id = tid
		controller.delete()

		then: 'the submitter will get deleted'
		controller.response.status == NO_CONTENT.value
		count == 1
		Submitter.count() == 0
	}

	// test update action
	void "update a submitter"() {
		given:
		Submitter s = Submitter.findOrCreateWhere(firstName: "Pedro", lastName: "Diegosa", emailAddress: "linuxmant@gmail.com", password: "asd").save(flush: true)

		when: 'updating the submitter'
		s.firstName = 'Diego'
		s.lastName = 'Pedrosa'
		s.password = '4 b3tter p4ss'
		controller.params.id = s.id
		controller.params.firstName = s.firstName
		controller.params.lastName = s.lastName
		controller.params.password = s.password
//		controller.request.json = s     // this doesn't work
		controller.update()

		// response.status is coming back 404 when using request.json, but object seems updated --- o.O
		then: 'it return the updated object in response.json and status should be 200'
		controller.response.status == OK.value
		Submitter.findByFirstNameAndLastName('Diego', 'Pedrosa') != null
		Submitter.findByFirstNameAndLastName('Pedro', 'Diegosa') == null
		Submitter.findByPassword("") == null
	}

	// test index action
	void "list 2 submitters"() {
		given:
		final int precount = Submitter.count
		new Submitter(firstName: "Gert", lastName: "Wohlgemuth", emailAddress: "wohlgemuth@ucdavis.edu", password: "dasdsa", spectra: null).save()
		new Submitter(firstName: "Diego", lastName: "Pedrosa", emailAddress: "linuxmant@gmail.com", password: "dsadasd", spectra: null).save()

		when: 'calling index action'
		controller.index()

		then: 'there should be 2 submitters in the json array and response should be 200(OK)'
		// before WE added items the table was empty
		precount == 0
		// check the response status
		controller.response.status == OK.value
		// right way -- this is testing the content of the response.json object created by the controller
		((JSONArray)controller.response.json).size() == 2
		// indirect way -- this is testing the domain class not the controller action
		Submitter.count == 2
	}

	// create and edit actions --  useless without web interface
}
