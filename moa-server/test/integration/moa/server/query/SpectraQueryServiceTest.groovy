package moa.server.query

import grails.test.spock.IntegrationSpec
import org.codehaus.groovy.grails.web.json.JSONArray
import spock.lang.Unroll

/**
 * Created by diego on 4/3/15.
 */
class SpectraQueryServiceTest extends IntegrationSpec {
	def spectraQueryService = new SpectraQueryService()
	def metaDataQueryService = new MetaDataQueryService()

	void setup() {
		spectraQueryService.metaDataQueryService = metaDataQueryService
	}

	def "quering with null json"() {

		when: "call query with null object"
		def res = spectraQueryService.query(null)

		then: "the service throws an exception"
		Exception e = thrown()

		expect: "and no results are returned"
		res == null
	}

	@Unroll
	def "query spectra.compound with #clazz #oper #value (max #size results)"() {

		expect:
		def res = spectraQueryService.query([compound: [(clazz): [(oper): value]]])
		println "Total: ${res.size()}"

		assert res != null
		assert res.size() >= size

		where:
		oper    | clazz  | value           | size
		"eq"    | "name" | "epicatechin 1" | 5
		"like"  | "name" | "epicatechin 1" | 5
		"ilike" | "name" | "epicatechin 1" | 5
		"gt"    | "name" | "epicatechin 1" | 5
		"lt"    | "name" | "epicatechin 1" | 5
		"ge"    | "name" | "epicatechin 1" | 5
		"le"    | "name" | "epicatechin 1" | 5
		"ne"    | "name" | "epicatechin 1" | 5
//		"in"    | "name"     | "epicatechin 1"                  | 5
		"eq" | "inchiKey" | "PFTAWBLQPZVEMU-UKRRQHHQSA-N" | 5
		"like" | "inchiKey" | "PFTAWBLQPZVEMU-UKRRQHHQSA-N" | 5
		"ilike" | "inchiKey" | "PFTAWBLQPZVEMU-UKRRQHHQSA-N" | 5
		"gt" | "inchiKey" | "PFTAWBLQPZVEMU-UKRRQHHQSA-N" | 5
		"lt" | "inchiKey" | "PFTAWBLQPZVEMU-UKRRQHHQSA-N" | 5
		"ge" | "inchiKey" | "PFTAWBLQPZVEMU-UKRRQHHQSA-N" | 5
		"le" | "inchiKey" | "PFTAWBLQPZVEMU-UKRRQHHQSA-N" | 5
		"ne" | "inchiKey" | "PFTAWBLQPZVEMU-UKRRQHHQSA-N" | 5
//		"in"    | "inchiKey" | "PFTAWBLQPZVEMU-UKRRQHHQSA-N"    | 5
		"eq" | "id" | 11591 | 5
		"gt" | "id" | 58 | 5
		"lt" | "id" | 58 | 5
		"ge" | "id" | 58 | 5
		"le" | "id" | 58 | 5
		"ne" | "id" | 58 | 5
	}

	@Unroll
	def "query spectra.tags #value (max #size results)"() {

		expect:
		def res = spectraQueryService.query([tags: value as JSONArray])
		println "Total: ${res.size()}"

		assert res != null
		assert res.size() == size

		where:
		value             | limit | size
		["megan"]         | 5     | 50
		["LCMS"]          | 5     | 0
		["duplicated",
		 "has M-15",
		 "GCMS",
		 "suspect value",
		 "noisy spectra"] | 5     | 480
	}

	@Unroll
	def "query spectra.metadata with #clazz #oper #value (max #size results)"() {

		expect:
		def res = spectraQueryService.query([metadata: [[(clazz): [(oper): value]]]])
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
	def "query multiple spectra.metadata with #clazz #oper1 #value, #clazz2 #oper2 #val2 (max #size results)"() {

		expect:
		def res = spectraQueryService.query([metadata: [[(clazz): [(oper1): value], (clazz2): [(oper2): val2]]]])
		println "Total: ${res.size()}"

		assert res != null
		assert res.size() >= size
		assert total == res.size()

		where:
		oper1 | oper2   | clazz  | value     | size | clazz2  | val2  | total
		"eq"  | "eq"    | "name" | "ms type" | 5    | "value" | "MS2" | 1441
		"eq"  | "like"  | "name" | "ms type" | 5    | "value" | "MS2" | 1441
		"eq"  | "ilike" | "name" | "ms type" | 5    | "value" | "MS2" | 1441
		"eq"  | "gt"    | "name" | "ms type" | 0    | "value" | "MS2" | 0
		"eq"  | "lt"    | "name" | "ms type" | 5    | "value" | "MS2" | 1641
		"eq"  | "ge"    | "name" | "ms type" | 5    | "value" | "MS2" | 1441
		"eq"  | "le"    | "name" | "ms type" | 5    | "value" | "MS2" | 3082
		"eq"  | "ne"    | "name" | "ms type" | 5    | "value" | "MS2" | 1641
//		"eq"    | "in"    | "name"     | "ms type"             | 5    | "value"    | "MS2"      | 5
		"eq" | "eq" | "category" | "spectral properties" | 5 | "id" | 64 | 1401
		"eq" | "gt" | "category" | "spectral properties" | 5 | "id" | 64 | 3216
		"eq" | "lt" | "category" | "spectral properties" | 5 | "id" | 346269 | 1401
		"eq" | "ge" | "category" | "spectral properties" | 5 | "id" | 64 | 3660
		"eq" | "le" | "category" | "spectral properties" | 5 | "id" | 64 | 1401
		"eq" | "ne" | "category" | "spectral properties" | 5 | "id" | 64 | 3216
//		"eq"    | "in"    | "category" | "spectral properties" | 5    | "id"       | 64         | 5
		"eq" | "eq" | "id" | 58 | 5 | "name" | "ms type" | 3082
		"eq" | "like" | "id" | 58 | 5 | "name" | "ms type" | 3082
		"eq" | "ilike" | "id" | 58 | 5 | "name" | "ms type" | 3082
		"eq" | "gt" | "id" | 58 | 0 | "name" | "ms type" | 0
		"eq" | "lt" | "id" | 58 | 0 | "name" | "ms type" | 0
		"eq" | "ge" | "id" | 58 | 5 | "name" | "ms type" | 3082
		"eq" | "le" | "id" | 58 | 5 | "name" | "ms type" | 3082
		"eq" | "ne" | "id" | 58 | 0 | "name" | "ms type" | 0
	}

	@Unroll
	void "find similar spectra to spectrum.id: 34129 using #type"() {

		when:
		def res = spectraQueryService.findSimilarSpectraIds(data, simil, ions, maxRes)

		then:
		assert res != null
		assert 10 >= res.size()

		where:
		type << ["id", "spectra"]
		simil << [500, 500]
		ions << [3, 3]
		maxRes << [10, 10]
		data << [34129, "85.0000:0.0267 " +
				"87.0000:0.0111 " +
				"89.0000:0.0427 " +
				"90.0000:0.0200 " +
				"91.0000:0.0711 " +
				"93.0000:0.0301 " +
				"101.0000:0.0100 " +
				"103.0000:0.0363 " +
				"104.0000:0.0520 " +
				"105.0000:0.0670 " +
				"115.0000:0.0303 " +
				"119.0000:0.0905 " +
				"120.0000:0.0141 " +
				"121.0000:0.0107 " +
				"126.0000:0.0158 " +
				"131.0000:0.0264 " +
				"132.0000:0.0072 " +
				"133.0000:0.0515 " +
				"135.0000:0.0829 " +
				"136.0000:0.0104 " +
				"140.0000:0.0388 " +
				"141.0000:0.0164 " +
				"147.0000:1.0000 " +
				"148.0000:0.1605 " +
				"149.0000:0.0903"]
	}
}
