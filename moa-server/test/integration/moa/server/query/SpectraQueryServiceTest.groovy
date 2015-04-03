package moa.server.query

import moa.Spectrum
import org.apache.log4j.Logger

/**
 * Created by diego on 4/2/15.
 */
class SpectraQueryServiceTest extends GroovyTestCase {
	private Logger log = Logger.getLogger(this.class.name)

	def spectraQueryService
	def metaDataQueryService

	void setUp() {
		super.setUp()

		metaDataQueryService = new MetaDataQueryService()
		spectraQueryService = new SpectraQueryService()
		spectraQueryService.metaDataQueryService = metaDataQueryService
	}

	//dumb test but the only solution when the ids change after db re-population
	void testQueryById() {
		def id = Spectrum.first().id

		assert id.equals(spectraQueryService.query(id).id)
	}

	void testQueryWithCompoundNameJsonShort() {
		def json = [compound: [name: "epicatechin 1"]]

		def res = spectraQueryService.query(json, [max: 10])

		assert res.size() > 0
	}

	void testQueryWithCompoundNameJsonLong() {
		def json = [compound: [name: [eq: "epicatechin 1"]]]

		def res = spectraQueryService.query(json, [max: 10])

		res.each { Spectrum sp ->
			log.debug(sp.id + "\n\t" + sp.biologicalCompound.names.toListString() + "\n\t" + sp.chemicalCompound.names.toListString())
		}

		assert res.size() > 0
	}

	void testQueryWithCompoundInchikeyJsonShort() {
		def json = [compound: [inchiKey: "PFTAWBLQPZVEMU-UKRRQHHQSA-N"]]

		def res = spectraQueryService.query(json, [max: 10])

		assert res.size() > 0
	}

	void testQueryWithCompoundInchikeyJsonLong() {
		def json = [compound: [inchiKey: [eq: "PFTAWBLQPZVEMU-UKRRQHHQSA-N"]]]

		def res = spectraQueryService.query(json, [max: 10])

		res.each { Spectrum sp ->
			log.debug(sp.id + "\n\t" + sp.biologicalCompound.inchiKey + "\n\t" + sp.chemicalCompound.inchiKey)
		}

		assert res.size() > 0
	}

	void testQueryWithCompoundIdJsonShort() {
		def json = [compound: [id: 4374]]

		def res = spectraQueryService.query(json, [max: 10])

		assert res.size() > 0
	}

	void testQueryWithCompoundIdJsonLong() {
		def json = [compound: [id: [eq: 4374]]]

		def res = spectraQueryService.query(json, [max: 10])

		res.each { Spectrum sp ->
			log.debug(sp.id + "\n\t" + sp.biologicalCompound.inchiKey + "\n\t" + sp.chemicalCompound.inchiKey)
		}

		assert res.size() > 0
	}

	//----------- similarity tests -----------
	void testFindSimilarSpectraIdsWithDefaults() {
		def ids = Spectrum.findAll().id

		def id = "15175"

		def res = spectraQueryService.findSimilarSpectraIds(id)

		assert res != null
		assert res.size() > 0
	}

}
