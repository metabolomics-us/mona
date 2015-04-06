package moa.server.query

import grails.test.spock.IntegrationSpec
import org.apache.log4j.Logger
import spock.lang.Unroll
/**
 * Created by diego on 3/31/15.
 */
class MetaDataQueryServiceTest extends IntegrationSpec {
	private Logger log = Logger.getLogger(this.class.name)

	def MetaDataQueryService metaDataQueryService = new MetaDataQueryService()

	void testQueryNullJson() {

		when:
		metaDataQueryService.query(null, [:])

		then: "the service throws an exception"
		Exception e = thrown()

	}

	@Unroll
	def "query spectra.metadata with #clazz #oper #value (max #size results)"() {

		expect:
		def res = metaDataQueryService.query([(clazz): [(oper): value]])
		println "Total: ${res.size()}"

		assert res != null
		assert res.size() >= size

		where:
		oper    | clazz  | value     | size | total
		"eq"    | "name" | "ms type" | 5    | 5
		"like"  | "name" | "ms type" | 5    | 5
		"ilike" | "name" | "ms type" | 5    | 5
		"gt"    | "name" | "ms type" | 5    | 5
		"lt"    | "name" | "ms type" | 5    | 5
		"ge"    | "name" | "ms type" | 5    | 5
		"le"    | "name" | "ms type" | 5    | 5
		"ne"    | "name" | "ms type" | 5    | 5
//		"in"    | "name"     | "ms type"             | 5    | 5
		"eq" | "value" | "MS2" | 5 | 5    //should fail -- doesn't match schema (needs 'name')
		"like" | "value" | "MS2" | 5 | 5
		"ilike" | "value" | "MS2" | 5 | 5
		"gt" | "value" | "MS2" | 5 | 5
		"lt" | "value" | "MS2" | 5 | 5
		"ge" | "value" | "MS2" | 5 | 5
		"le" | "value" | "MS2" | 5 | 5
		"ne" | "value" | "MS2" | 5 | 5
//		"in"    | "value"    | "MS2"                 | 5    | 5
		"eq" | "category" | "spectral properties" | 5 | 5
		"like" | "category" | "spectral properties" | 5 | 5
		"ilike" | "category" | "spectral properties" | 5 | 5
		"gt" | "category" | "spectral properties" | 0 | 5
		"lt" | "category" | "spectral properties" | 5 | 5
		"ge" | "category" | "spectral properties" | 5 | 5
		"le" | "category" | "spectral properties" | 5 | 5
		"ne" | "category" | "spectral properties" | 5 | 5
//		"in"    | "category" | "spectral properties" | 5    | 5
		"eq" | "id" | 58 | 5 | 5
		"gt" | "id" | 58 | 5 | 5
		"lt" | "id" | 58 | 5 | 5
		"ge" | "id" | 58 | 5 | 5
		"le" | "id" | 58 | 5 | 5
		"ne" | "id" | 58 | 5 | 5
	}

	@Unroll
	def "query metadata with limit of #limit"() {

		expect:
		def res = metaDataQueryService.query([(clazz): [(oper1): value]], [max: limit])
		println "Total: ${res.size()}"

		assert res != null
		assert res.size() == limit

		where:
		oper1 | clazz      | value                 | size | limit
		"eq"  | "name"     | "ms type"             | 5    | 3
		"eq"  | "category" | "spectral properties" | 5    | 3
		"eq"  | "id"       | 58                    | 5    | 3
	}

}
