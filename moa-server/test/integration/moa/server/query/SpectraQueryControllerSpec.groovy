package moa.server.query

import grails.test.spock.IntegrationSpec
import org.apache.log4j.Logger
import org.springframework.http.HttpMethod
import spock.lang.Shared
import spock.lang.Unroll

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

		controller.request.contentType = 'application/json'
		controller.request.format = 'json'
		controller.request.parameters = [max:"5"]
		controller.request.method = 'POST'
		controller.request.requestMethod = HttpMethod.POST
		controller.response.format = 'json'
	}

	def cleanup() {
		controller.request.json = [:]
	}

	@Unroll
	void "Query should validate with #query"() {
		expect:
		callRest(query)

		controller.response.status == 200
		controller.response.contentAsString.contains(content)

		!controller.response.contentAsString.contains("missing:)")

		where:
		content                                         | query
		"\"inchiKey\": \"PFTAWBLQPZVEMU-DZGCQCFKSA-N\"" | [compound:[name:[eq:"catechin"]]]
		"\"name\": \"ms type\","                        | [metadata:[[name:[like:"ms type"]]]]
		"\"text\": \"duplicated\""                      | [tags:["duplicated"]]
		"\"text\": \"noisy spectra\""                   | [tags:["duplicated", "noisy spectra"]]

	}

	@Unroll
	void "Query should fail with bad #query"() {
		expect:
		callRest(query)

		controller.response.status == 400
		controller.response.contentAsString.contains(error)

		!controller.response.contentAsString.contains(content)

		where:
		content                                         | query                         | error
		"\"name\": \"ms type\","	                    | [metadata:[[value:"MS2"]]]	| "schema requires [\"name\"]; missing: [\"name\"])"
		"\"inchiKey\": \"QNAYBMKLOCPYGJ-REOHCLBHSA-N\"" | [compound:[name:"alanine"]]   | "instance failed to match exactly one schema"

	}

	private boolean callRest(Map query) {
		controller.request.json = query
		controller.search()

		return true
	}
}
