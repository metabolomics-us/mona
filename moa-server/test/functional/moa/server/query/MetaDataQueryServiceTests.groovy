package moa.server.query

import grails.converters.JSON
import grails.plugins.rest.client.RestBuilder
import grails.plugins.rest.client.RestResponse
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.web.json.JSONObject
import spock.lang.Shared
import spock.lang.Specification

public class MetaDataQueryServiceTests extends Specification {

	@Shared
	String baseUrl = "http://localhost:8080/rest"

	MetaDataQueryService service
	Logger logger = Logger.getLogger(this.class.name)


	public void setup() throws Exception {
	}

	def 'should query some metadata'() {
		given:
		RestBuilder rest = new RestBuilder()

		when:
		RestResponse response = rest.post("$baseUrl/meta/data/search") {
			contentType 'application/json'

			json '{value:"MT000001.txt"}'
		}

		then:
		logger.error("json: $response.json")
		assert response.status == 200
		assert response.json.length() == 1
		assert "none".equals(response.json[0].category)
		assert "origin".equals(response.json[0].name)
		assert "string".equals(response.json[0].type)
		assert JSONObject.NULL == response.json[0].unit
		assert "MT000001.txt".equals(response.json[0].value)
	}

	def 'should query some spectra by metadata (full syntax)'() {
		given:
		RestBuilder rest = new RestBuilder()

		when:
		RestResponse response = rest.post("$baseUrl/spectra/search") {
			contentType 'application/json'
			accept JSON

			json '{metadata:[{value:{eq:"MT000002.txt"}}]}'
		}

		then:
		logger.error("json: $response.json")
		logger.error("size: ${response.json.length()}")
		assert response.status == 200
		assert response.json != null
		assert response.json.length() == 1
		assert 512 == response.json[0].id
		assert "KWIUHFFTVRNATP-UHFFFAOYSA-N".equals(response.json[0].biologicalCompound.inchiKey)
	}

	def 'should query some spectra by metadata (short syntax)'() {
		given:
		RestBuilder rest = new RestBuilder()

		when:
		RestResponse response = rest.post("$baseUrl/spectra/search") {
			contentType 'application/json'
			accept JSON

			json '{metadata:[{value:"MT000002.txt"}]}'
		}

		then:
		logger.error("json: $response.json")
		logger.error("size: ${response.json.length()}")
		assert response.status == 200
		assert response.json != null
		assert response.json.length() == 1
		assert 512 == response.json[0].id
		assert "KWIUHFFTVRNATP-UHFFFAOYSA-N".equals(response.json[0].biologicalCompound.inchiKey)
	}

	def 'should query some spectra by spectra keyword'() {
		given:
		RestBuilder rest = new RestBuilder()

		when:
		RestResponse response = rest.post("$baseUrl/spectra/search") {
			contentType 'application/json'
			accept JSON

			json '{spectrum:"89.1:100.0"}'
		}

		then:
		logger.error("json: $response.json")
		logger.error("size: ${response.json.length()}")
		assert response.status == 200
		assert response.json != null
	}
}
