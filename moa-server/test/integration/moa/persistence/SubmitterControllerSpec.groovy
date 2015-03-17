package moa.persistence

import grails.converters.JSON
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
    void "save a submitter from params"() {
        given:
        final int count = Submitter.count()

        when: 'saving a new submitter'
        controller.params.firstName = 'Test'
        controller.params.lastName = 'User'
        controller.params.institution = 'UC Davis'
        controller.params.emailAddress = 'test.user@ucdavis.edu'
        controller.params.password = 'password'
        controller.save()

        then: 'it should be persisted'
        controller.response.status == CREATED.value
        Submitter.count() == count + 1
        Submitter.findByEmailAddress('test.user@ucdavis.edu').firstName == 'Test'
        Submitter.findByEmailAddress('test.user@ucdavis.edu').lastName == 'User'
    }

	void "save a submitter from a json string"() {
		given:
		final int count = Submitter.count()

		when: 'saving a new submitter'
		controller.request.json = """
{
    "firstName": "Test",
    "lastName": "User",
    "institution": "UC Davis",
    "emailAddress": "test.user@ucdavis.edu",
    "password": "password"
}
"""
		controller.save()

		then: 'it should be persisted'
		controller.response.status == CREATED.value
        Submitter.count() == count + 1
		Submitter.findByEmailAddress('test.user@ucdavis.edu').firstName == 'Test'
		Submitter.findByEmailAddress('test.user@ucdavis.edu').lastName == 'User'
	}

    void "save a submitter using domain object"() {
        given:
        final int count = Submitter.count()

        when: 'saving a new submitter'
        new Submitter(firstName: 'Test', lastName: 'User', institution: 'UC Davis', emailAddress: 'test.user@ucdavis.edu', password: 'password').save(flush: true)

        then: 'it should be persisted'
        Submitter.count() == count + 1
        Submitter.findByEmailAddress('test.user@ucdavis.edu').firstName == 'Test'
        Submitter.findByEmailAddress('test.user@ucdavis.edu').lastName == 'User'
    }


	// test show action
	void "show a submitter"() {
		given:
        final int count = Submitter.count()
        def submitter = new Submitter(firstName: 'Test', lastName: 'User', institution: 'UC Davis', emailAddress: 'test.user@ucdavis.edu', password: 'password').save(flush: true)

        when: 'showing one submitter'
        controller.params.id = submitter.id
		controller.show()

		then: 'the response status should be 200(OK) and there should be 1 item in the json response'
		Submitter.count() == count + 1
		controller.response.status == OK.value

        controller.response.json.containsKey('firstName') && controller.response.json.get('firstName') == 'Test'
        controller.response.json.containsKey('lastName') && controller.response.json.get('lastName') == 'User'
        controller.response.json.containsKey('emailAddress') && controller.response.json.get('emailAddress') == 'test.user@ucdavis.edu'
        controller.response.json.containsKey('institution') && controller.response.json.get('institution') == 'UC Davis'
	}

	// test delete action
	void "delete a submitter"() {
		given:
        def submitter = new Submitter(firstName: 'Test', lastName: 'User', institution: 'UC Davis', emailAddress: 'test.user@ucdavis.edu', password: 'password').save(flush: true)
        final int count = Submitter.count()

		when: 'calling the delete action with parameter id'
        controller.params.id = submitter.id
        controller.delete()

		then: 'the submitter will get deleted'
		controller.response.status == NO_CONTENT.value
		Submitter.count() == count - 1
	}

	// test update action
	void "update a submitter"() {
		given:
        final int count = Submitter.count()
        def s = new Submitter(firstName: 'Test', lastName: 'User', institution: 'UC Davis', emailAddress: 'test.user@ucdavis.edu', password: 'password').save(flush: true)

		when: 'updating the submitter'
		controller.params.id = s.id
		controller.params.firstName = 'MoNA'
		controller.params.lastName = 'Tester'
        controller.params.institution = 'UC San Diego'
		controller.update()

		// response.status is coming back 404 when using request.json, but object seems updated --- o.O
		then: 'it return the updated object in response.json and status should be 200'
		controller.response.status == OK.value
		Submitter.findByFirstNameAndLastName('Test', 'User') == null
		Submitter.findByFirstNameAndLastName('MoNA', 'Tester') != null
        Submitter.findByInstitution('UC Davis') == null
        Submitter.findByInstitution('UC San Diego') != null
	}

	// test index action
	void "list 2 submitters"() {
		given:
        final int count = Submitter.count()
        new Submitter(firstName: 'Test', lastName: 'User', institution: 'UC Davis', emailAddress: 'test.user@ucdavis.edu', password: 'password').save(flush: true)
        new Submitter(firstName: 'MoNA', lastName: 'Tester', institution: 'UC Davis', emailAddress: 'test.user.2@ucdavis.edu', password: 'password').save(flush: true)

		when: 'calling index action'
		controller.index()

		then: 'there should be 2 submitters in the json array and response should be 200(OK)'
        // check the response status
        controller.response.status == OK.value
        Submitter.count() == count + 2

		// right way -- this is testing the content of the response.json object created by the controller
        controller.response.json.size() == count + 2

        Submitter.findByFirstNameAndLastName('Test', 'User') != null
        Submitter.findByFirstNameAndLastName('MoNA', 'Tester') != null
	}
}
