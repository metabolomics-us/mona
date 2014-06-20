package moa.persistence

import grails.test.spock.IntegrationSpec
import moa.Tag
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.web.json.JSONArray

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
class TagControllerSpec extends IntegrationSpec {
	Logger logger = Logger.getLogger(this.class)
	TagController controller = new TagController()
	Tag tag

	def setup() {
		controller.request.contentType = "text/json"
		controller.response.format = "json"
	}

	def cleanup() {
		Tag.deleteAll(Tag.list())
	}

	void "create a tag"() {

		when:
		controller.request.json = new Tag(text: "this is a simple tag")
		controller.save()

		then:
		controller.response.status == 201
		Tag.count() == 1
	}

	void "delete the tag"() {

		given:
		def t = new Tag(text:'bye')
		t.save(flush: true)

		when:
		controller.request.method = 'DELETE'
		controller.request.json = t
		logger.error("request: " + controller.request.JSON)
		logger.error("request: " + controller.request.method)
		controller.delete()
		controller.request.each {
			logger.error(it)
		}
		logger.error("response: " + controller.response.status)

		then:
		Tag.count() == 0

	}

	void "update a tag"() {
		given:
		Tag t = Tag.findOrCreateWhere(text: 'bad tug').save(flush:true)

		when: 'updating the tag'
		t.text = 'good tag'
		controller.request.method = 'PUT'
		controller.request.json = t
		controller.update()
		controller.request.each {
			logger.error(it)
		}
		controller.response.each {
			logger.error(it)
		}

		// response.status is coming back 404, but object seems updated --- o.O
		then: 'it return the updated object in response.json and status should be 200'
		controller.response.status == 200
		Tag.findByText('good tag') != null
		Tag.findByText('bad tug') == null
	}

	void "list all tags"() {

		given:
		final int precount = Tag.list().size()
		new Tag(text: "1").save(flush: true)
		new Tag(text: "2").save(flush: true)

		when:
		controller.index()

		then: 'there should be 2 items in the json array and response status should be 200(OK)'
		// before WE added items the table was empty
		precount == 0
		// check the response status
		controller.response.status == 200
		// right way -- this is testing the content of the response.json object created by the controller
		((JSONArray)controller.response.json).size() == 2
		// indirect way -- this is testing the domain class not the controller action
		Tag.list().size() == 2
	}
}