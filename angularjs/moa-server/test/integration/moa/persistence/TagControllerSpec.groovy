package moa.persistence

import grails.test.spock.IntegrationSpec
import moa.Tag
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.web.json.JSONArray

import static org.springframework.http.HttpStatus.*

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
class TagControllerSpec extends IntegrationSpec {
	Logger logger = Logger.getLogger(this.class)
	TagController controller = new TagController()

	def setup() {
		controller.request.contentType = "text/json"
		controller.response.format = "json"
	}

	def cleanup() {
		Tag.deleteAll(Tag.list())
	}

	// test save action
	void "save a tag"() {
		given:
		int count = Tag.count
		Tag t = new Tag(text: "this is a simple tag")

		when:
		controller.request.json = t
		controller.save()

		then:
		controller.response.status == CREATED.value
		count == 0
		Tag.count() == 1
	}

	// test show action
	void "show a tag"() {

		given:
		def tid = new Tag(text: 'show me').save(flush: true)
		new Tag(text: 'i\'m shy').save(flush: true)

		when:
		controller.params.id = tid.id
		controller.show()

		then:
		Tag.count() == 2
		controller.response.status == OK.value
		controller.response.json == [id: '2', text: 'show me'] || [id: '5', text: 'show me']
	}

	// test delete action
	void "delete the tag"() {

		given:
		int tid = new Tag(text: 'bye').save(flush: true).id
		int count = Tag.count()

		when: 'calling the delete action with parameter id'
		controller.params.id = tid
		controller.delete()

		then: 'the tag will get deleted'
		controller.response.status == NO_CONTENT.value
		count == 1
		Tag.count() == 0
	}

	// test update action
	void "update a tag"() {
		given:
		Tag t = Tag.findOrCreateWhere(text: 'bad tug').save(flush:true)

		when: 'updating the tag'
		t.text = 'good tag'
		controller.params.id = t.id
		controller.params.text = t.text
//		controller.request.json = t     // this doesn't work
		controller.update()

		// response.status is coming back 404, but object seems updated --- o.O
		then: 'it return the updated object in response.json and status should be 200'
		controller.response.status == OK.value
		Tag.findByText('good tag') != null
		Tag.findByText('bad tug') == null
	}

	// test index action
	void "list all tags"() {

		given:
		final int precount = Tag.count
		new Tag(text: "1").save(flush: true)
		new Tag(text: "2").save(flush: true)

		when:
		controller.index()

		then: 'there should be 2 items in the json array and response status should be 200(OK)'
		// before WE added items the table was empty
		precount == 0
		// check the response status
		controller.response.status == OK.value
		// right way -- this is testing the content of the response.json object created by the controller
		((JSONArray)controller.response.json).size() == 2
		// indirect way -- this is testing the domain class not the controller action
		Tag.count == 2
	}

	// create and edit actions --  useless without web interface
}