package moa.server.query

import grails.test.spock.IntegrationSpec
import org.apache.log4j.Logger
import org.springframework.http.HttpMethod
import spock.lang.Shared

/**
 * Created by diego on 4/7/15.
 */
class SpectraQueryControllerSpec extends IntegrationSpec {
	def Logger log = Logger.getLogger(this.class)

	@Shared
	SpectraQueryController controller = new SpectraQueryController()
	def spectraQueryService
	def spectraConversionService

	def setup() {
		controller.spectraQueryService = spectraQueryService
		controller.spectraConversionService = spectraConversionService
	}

	void "test controller Search"() {
		setup:
		def json = [metadata:[[name:[like:"ms type"]]]]
		controller.request.contentType = 'text/json'
		controller.request.format = 'json'
		controller.response.format = 'json'
		controller.request.method = 'POST'
		controller.request.requestMethod = HttpMethod.POST

		when:
		controller.request.json = json
		controller.search()
		def res = controller.response.json

		then:
		log.info("result ${res}")

		controller.response.status == 200

	}
}
