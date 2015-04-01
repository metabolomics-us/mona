package moa.server.query

import moa.MetaDataValue
import org.apache.log4j.Logger
import org.junit.Before
/**
 * Created by diego on 3/31/15.
 */
class MetaDataQueryServiceTest extends GroovyTestCase {
	private static final Logger log = Logger.getLogger("(Integration) MetaDataQueryServiceTest")

	def MetaDataQueryService metaDataQueryService

	@Before
	void setUp() {
		metaDataQueryService = new MetaDataQueryService()
	}

	void testQueryById() {
		//getting id
		long id = MetaDataValue.first().id


		MetaDataValue res = metaDataQueryService.query(id)

		log.info("RESULT: $res")
		assert res != null
		assert res.id == id
	}

	void testQueryShortJson() {
		def json = [name:"ms type", value:"MS2"]
		def params = [max:10]

		def res = metaDataQueryService.query(json, params)

		log.debug("RES: ${res.class.simpleName}")

		assert res != null
	}

	void testQueryLongJson() {
		def json = [name:["like":"ms type"], value:["eq":"MS2"]]
		def params = [max:10]

		def res = metaDataQueryService.query(json, params)

		log.debug("RES: ${res.class.simpleName}")

		assert res != null
	}

//	void testQuery3() {
//
//	}
//
//	void testQuery4() {
//
//	}
}
