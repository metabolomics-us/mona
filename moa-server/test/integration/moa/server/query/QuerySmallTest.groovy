package moa.server.query

import grails.test.spock.IntegrationSpec
import spock.lang.Unroll

/**
 * Created by diego on 4/7/15.
 */
class QuerySmallTest extends IntegrationSpec{
	def spectraQueryService

	@Unroll
	void "run query with textual data (#op)"() {

		expect:
		def res = spectraQueryService.query([compound: [
														name: [(op): "epicatechin"],
		                                                inchiKey:[(op):"PFTAWBLQPZVEMU-UKRRQHHQSA-N"]
//		                                                metadata:[[name:[(op):"ms type"]]]
											]])

		assert res.size() > 0

		where:
		op      | _
		"eq"    | _
		"like"  | _
		"ilike" | _
		"gt"    | _
		"lt"    | _
		"ge"    | _
		"le"    | _
		"ne"    | _
	}

	@Unroll
	void "run query with numerical data (#op)"() {
		expect:
		def res = spectraQueryService.query([compound: [ id:[(op):11591] ]])

		assert res.size() > 0

		where:
		op      | _
		"eq"    | _
		"gt"    | _
		"lt"    | _
		"ge"    | _
		"le"    | _
		"ne"    | _
	}

	@Unroll
	void "run query with numerical data and bad operators (like, ilike)"() {

		when:
		def res = spectraQueryService.query([compound: [id:[like:11591]]])

		then:
		def e = thrown(RuntimeException)
		assert res == null
		assert e.message.equals("Can't use 'like' or 'ilike with numeric data")

		when:
		res = spectraQueryService.query([compound: [id:[ilike:11591]]])

		then:
		e = thrown(RuntimeException)
		assert res == null
		assert e.message.equals("Can't use 'like' or 'ilike with numeric data")
	}
}
