package moa.server.query
import grails.test.spock.IntegrationSpec
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.web.json.JSONArray
import spock.lang.Unroll
/**
 * Created by diego on 4/3/15.
 */
class SpectraQueryServiceSpec extends IntegrationSpec {
	private final Logger log = Logger.getLogger(this.class.name)

	def spectraQueryService = new SpectraQueryService()
	def metaDataQueryService = new MetaDataQueryService()

	def setup() {
		spectraQueryService.metaDataQueryService = metaDataQueryService
	}

	void "quering with null json"() {

		when: "call query with null object"
		def res = spectraQueryService.query(null)

		then: "the service throws an exception"
		Exception e = thrown()

		expect: "and no results are returned"
		res == null
	}

//	@Ignore
	@Unroll
	void "query spectra.compound with #clazz #oper #value (max #size results)"() {

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
		"eq" | "id" | 349 | 5
		"gt" | "id" | 349 | 5
		"lt" | "id" | 349 | 5
		"ge" | "id" | 349 | 5
		"le" | "id" | 349 | 5
		"ne" | "id" | 349 | 5
	}

//	@Ignore
	@Unroll
	void "query spectra.tags #value (max #size results)"() {

		expect:
		def res = spectraQueryService.query([tags: value as JSONArray])
		println "Total: ${res.size()}"

		assert res != null
		assert res.size() == size

		where:
		value             | limit | size
		["noisy spectra"] | 5 | 3953
		["LCMS"]          | 5 | 4
		["duplicated",
		 "LCMS",
		 "suspect value",
		 "noisy spectra"] | 5 | 2
	}

//	@Ignore
	@Unroll
	void "query spectra.metadata with #clazz #oper #value (max #size results)"() {

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
		"eq" | "id" | 79 | 5 | 5
		"gt" | "id" | 79 | 5 | 5
		"lt" | "id" | 79 | 5 | 5
		"ge" | "id" | 79 | 5 | 5
		"le" | "id" | 79 | 5 | 5
		"ne" | "id" | 79 | 5 | 5
	}

//	@Ignore
	@Unroll
	void "query multiple spectra.metadata with #clazz #oper1 #value, #clazz2 #oper2 #val2"() {

		expect:
		def res = spectraQueryService.query([metadata: [[(clazz): [(oper1): value], (clazz2): [(oper2): val2]]]])
		log.debug "Total: ${res.size()}"

		assert res != null
		assert res.size() >= size
		assert total == res.size()

		where:
		oper1 | oper2   | clazz  | value     | size | clazz2  | val2  | total
		"eq"  | "eq"    | "name" | "ms type" | 5    | "value" | "MS2" | 2938
		"eq"  | "like"  | "name" | "ms type" | 5    | "value" | "MS2" | 2938
		"eq"  | "ilike" | "name" | "ms type" | 5    | "value" | "MS2" | 2938
		"eq"  | "gt"    | "name" | "ms type" | 0    | "value" | "MS2" | 0
		"eq"  | "lt"    | "name" | "ms type" | 5    | "value" | "MS2" | 1624
		"eq"  | "ge"    | "name" | "ms type" | 5    | "value" | "MS2" | 2938
		"eq"  | "le"    | "name" | "ms type" | 5    | "value" | "MS2" | 4562
		"eq"  | "ne"    | "name" | "ms type" | 5    | "value" | "MS2" | 1624
//		"eq"  | "in"    | "name"     | "ms type"            | 5    | "value" | "MS2"     | 5
		"eq" | "eq" | "category" | "chromatography" | 4 | "id" | 323384 | 4
		"eq" | "gt" | "category" | "chromatography" | 4 | "id" | 323384 | 4
		"eq" | "lt" | "category" | "chromatography" | 4 | "id" | 323384 | 4
		"eq" | "ge" | "category" | "chromatography" | 4 | "id" | 323384 | 4
		"eq" | "le" | "category" | "chromatography" | 4 | "id" | 323384 | 4
		"eq" | "ne" | "category" | "chromatography" | 4 | "id" | 323384 | 4
//		"eq"  | "in"    | "category" | "chromatography"     | 5    | "id"    | 323384    | 1
		"eq" | "eq" | "id" | 113 | 5 | "name" | "ms type" | 4562
		"eq" | "like" | "id" | 113 | 5 | "name" | "ms type" | 4562
		"eq" | "ilike" | "id" | 113 | 5 | "name" | "ms type" | 4562
		"eq" | "gt" | "id" | 113 | 0 | "name" | "ms type" | 0
		"eq" | "lt" | "id" | 113 | 0 | "name" | "ms type" | 0
		"eq" | "ge" | "id" | 113 | 5 | "name" | "ms type" | 4562
		"eq" | "le" | "id" | 113 | 5 | "name" | "ms type" | 4562
		"eq" | "ne" | "id" | 113 | 0 | "name" | "ms type" | 0
	}

//	@Ignore
	@Unroll
	void "find similar spectra to spectrum.id: 34129 using #type"() {

		when:
		def res = spectraQueryService.findSimilarSpectraIds(spectrum, simil, ions, maxRes)

		then:
		assert res != null
		assert 2 == res.size()

		where:
		type = "spectra"
		simil = 500
		ions = 3
		maxRes = 10
		spectrum = "225.1853:0.6006 " +
				"379.1246:0.5005 " +
				"395.3887:0.6006 " +
				"397.1352:1.0000 " +
				"415.1457:0.2002 " +
				"605.3176:0.3003 " +
				"623.3281:0.3003 " +
				"775.5209:0.3003 " +
				"793.5314:0.3003 " +
				"1001.7140:0.5005 " +
				"1047.7195:0.1001"
	}

	@Unroll
	void "the validation is OK with #query"() {
		expect:
		def res = spectraQueryService.validateQuery(query)

		assert res != null
		assert res.success

		assert res[0] == null

		where:
		_ | query
		_ | [compound: [:], metadata: [], tags: []]  // returns all spectra
		_ | [compound:[name:[eq:'alanine']]]
		_ | [metadata:[[name:[like:"ms type"], value:[eq:"MS2"]]]]
		_ | [tags:["dirty", "fiehnlab"]]
		_ | [compound: [name:[like:"alanine"]], metadata:[[name:[eq:"ms type"], value:[like:"MS2"]]], tags: ["dirty"]]
	}

	@Unroll
	void "the validation should fail for #query"() {
		expect:
		def res = spectraQueryService.validateQuery(query)

		assert res != null
		assert !res.success

		log.debug("RESULT: $res\n${res[0].message}")
		assert !res[0].message.isEmpty()
		assert res[0].message.contains(error)

		where:
		query                                               | error
		[:]                                                 | "instance failed to match at least one required schema among 3"
		[metadata:[[name:"ms type"]]]                       | "instance failed to match exactly one schema"
		[metadata:[value:[eq:'MS2']]]                       | "instance type (object) does not match any allowed primitive type (allowed: [\"array\"])"
		[metadata:[[value:[eq:'MS2']]]]                     | "requires [\"name\"]; missing: [\"name\"]"
		[compound: [badkey: 'should break']] | "object instance has properties which are not allowed by the schema: [\"badkey\"]"
		[tags:[43]]                                         | "instance type (integer) does not match any allowed primitive type (allowed: [\"string\"])"
		[tags:"bad tags object"]                            | "instance type (string) does not match any allowed primitive type (allowed: [\"array\"])"
		[compound:[name:'alanine'], extraprop:'invalid']    | "object instance has properties which are not allowed by the schema"
	}
}
