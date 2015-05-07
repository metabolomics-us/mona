package moa.server.query

import grails.test.spock.IntegrationSpec
import org.apache.log4j.Logger
import org.springframework.http.HttpMethod
import spock.lang.Shared

/**
 * Created by diego on 4/23/15.
 */
class MetaDataQueryControllerSpec extends IntegrationSpec {
	def Logger log = Logger.getLogger(this.class)
	def metaDataQueryService

	@Shared MetaDataQueryController controller = new MetaDataQueryController()

	def setup() {
		controller.metaDataQueryService = metaDataQueryService
	}

	def cleanup() {	}

	void "Query metadata with good string"() {
		setup:
		def json = [name:[like:"ms type"]]
		controller.request.contentType = 'text/json'
		controller.request.format = 'json'
		controller.request.method = 'POST'
		controller.request.requestMethod = HttpMethod.POST

		controller.response.format = 'json'

		when:
		controller.request.json = json
		controller.query()
		def res = controller.response.json

		then:

		controller.response.status == 200
		controller.response.contentAsString.contains("\"name\": \"ms type\",")

		!controller.response.contentAsString.contains("missing:)")

	}

	void "Query metadata with bad string"() {
		setup:
		def json = [value:"MS2"]
		controller.request.contentType = 'text/json'
		controller.request.format = 'json'
		controller.request.method = 'POST'
		controller.request.requestMethod = HttpMethod.POST

		controller.response.format = 'json'

		when:
		controller.request.json = json
		controller.query()
		def res = controller.response.json

		then:

		controller.response.status == 400
		controller.response.contentAsString.contains("schema requires [\\\"name\\\"]; missing: [\\\"name\\\"])")

		!controller.response.contentAsString.contains("\"name\": \"ms type\",")
	}
}
