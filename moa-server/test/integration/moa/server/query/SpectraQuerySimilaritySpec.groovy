package moa.server.query

import grails.test.spock.IntegrationSpec
import org.apache.log4j.Logger
import org.springframework.http.HttpMethod
import spock.lang.Shared
import spock.lang.Unroll

/**
 * Test specification related to similarity searches
 * Created by diego on 5/4/15.
 */
class SpectraQuerySimilaritySpec extends IntegrationSpec {
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
		controller.request.parameters = [max: "5"]
		controller.request.method = 'POST'
		controller.request.requestMethod = HttpMethod.POST
		controller.response.format = 'json'
	}

	def cleanup() {
		controller.request.json = [:]
	}


	@Unroll
	void "should fail with bad query: #query"() {
		expect:
		callRest(query)

		controller.response.status >= 400
		controller.response.status < 500
		controller.response.contentAsString.contains(error)

		!controller.response.contentAsString.contains(content)

		where:
		content           | query                                                       | error
		"spectra: "       | [:]                                                         | "object has missing required properties ([\"minSimilarity\",\"spectra\"])"
		"spectra: "       | [spectra: ""]                                               | "object has missing required properties ([\"minSimilarity\"])"
		"minSimilarity: " | [minSimilarity: ""]                                         | "object has missing required properties ([\"spectra\"])"
		"spectra: "       | [spectra: "100:10", minSimilarity: "bad int"]               | "instance type (string) does not match any allowed primitive type (allowed: [\"integer\",\"number\"])"
		"spectra: "       | [spectra: "100:10", minSimilarity: 100, extraProp: "bad"]   | "object instance has properties which are not allowed by the schema: [\"extraProp\"]"
		"spectra: "       | [spectra: "100:10", minSimilarity: -1]                      | "numeric instance is lower than the required minimum (minimum: 0, found: -1)"
		"spectra: "       | [spectra: "100:10", minSimilarity: 1001]                    | "numeric instance is greater than the required maximum (maximum: 1000, found: 1001)"
		"spectra: "       | [spectra: "100:10", minSimilarity: 500, maxHits: -1]        | "numeric instance is lower than the required minimum (minimum: 0, found: -1)"
		"spectra: "       | [spectra: "100:10", minSimilarity: 500, maxHits: 26]        | "numeric instance is greater than the required maximum (maximum: 25, found: 26)"
		"spectra: "       | [spectra: "100:10", minSimilarity: 500, commonIonCount: -1] | "numeric instance is lower than the required minimum (minimum: 0, found: -1)"
	}

	@Unroll
	void "should pass with query: #query"() {
		expect:
		callRest(query)

		controller.response.status == 200

		def json = controller.response.json as Map

		json != null

		json.result.size() <= results

		def jres = json.result as ArrayList

		jres.each {
			others.contains(it)
		}

		where:
		query                                                                | results | others
		[spectra: 444893, minSimilarity: 900]                                | 5       | [[id: 447803, similarity: 1000.0], [id: 819873, similarity: 978.649535066521], [id: 819791, similarity: 978.649535066521]]
		[spectra: 444893, minSimilarity: 900, maxHits: 2]                    | 2       | [[id: 447803, similarity: 1000.0], [id: 819873, similarity: 978.649535066521], [id: 819791, similarity: 978.649535066521]]
		[spectra: 444893, minSimilarity: 900, commonIonCount: 5]             | 5       | [[id: 447803, similarity: 1000.0], [id: 819873, similarity: 978.649535066521], [id: 819791, similarity: 978.649535066521]]
		[spectra: 444893, minSimilarity: 900, maxHits: 2, commonIonCount: 5] | 2       | [[id: 447803, similarity: 1000.0], [id: 819873, similarity: 978.649535066521], [id: 819791, similarity: 978.649535066521]]
	}

	private boolean callRest(Map query) {
		controller.request.json = query
		controller.similaritySearch()

		return true
	}

}
