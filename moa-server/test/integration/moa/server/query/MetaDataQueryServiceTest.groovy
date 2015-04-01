package moa.server.query

import moa.MetaDataValue
import org.apache.log4j.Logger
import org.junit.Before

/**
 * Created by diego on 3/31/15.
 */
class MetaDataQueryServiceTest extends GroovyTestCase {
	private Logger log = Logger.getLogger(this.class.simpleName)

	def MetaDataQueryService metaDataQueryService

	@Before
	void setUp() {
		metaDataQueryService = new MetaDataQueryService()
	}

	void testQueryNullJson() {
		def params = [:]

		// should throw an Exception
		shouldFail(Exception) {
			metaDataQueryService.query(null, params)
		}

	}

	void testQueryByIdShort() {
		//getting id
		long id = MetaDataValue.first().id


		MetaDataValue res = metaDataQueryService.query(id)

		assert res != null
		assert res.id == id
	}

	void testQueryByIdLong() {
		//getting id
		long id = MetaDataValue.first().id

		def json = [id: ["like": id]]

		MetaDataValue res = metaDataQueryService.query(id)

		assert res != null
		assert res.id == id
	}

	void testQueryShortJson() {
		def json = [name:"ms type", value:"MS2"]
		def params = [max: 10, offset: 5]

		def res = metaDataQueryService.query(json, params)

		assert res != null
		assert 10 <= res.size()
		assert (res['stringValue'] as HashSet<String>).contains("MS2")
		assert (res['stringValue'] as HashSet<String>).size() == 1
	}

	void testQueryLongJson() {
		def json = [name:["like":"ms type"], value:["eq":"MS2"]]
		def params = [max:10]

		def res = metaDataQueryService.query(json, params)

		assert res != null
		assert 10 <= res.size()
		assert (res['stringValue'] as HashSet<String>).contains("MS2")
		assert (res['stringValue'] as HashSet<String>).size() == 1
	}

	void testQueryJsonBetween() {
		def json = [value: [between: ["0", "1000"]]]
		def params = [max: 10]

		def res = metaDataQueryService.query(json, params)

		assert res != null
		assert 10 <= res.size()
	}

	void testQueryCategoryShort() {
		def json = [category: "spectral properties"]
		def params = [max: 10]

		def res = metaDataQueryService.query(json, params)

		assert res != null
		assert res.size() <= 10
		assert "143".equals(res[0].value)
		assert "num peaks".equals(res[0].metaData.name)
	}

	void testQueryCategoryLong() {
		def json = [category: [eq: "spectral properties"]]
		def params = [max: 10]

		def res = metaDataQueryService.query(json, params)

		assert res != null
		assert res.size() <= 10
		assert "143".equals(res[0].value)
		assert "num peaks".equals(res[0].metaData.name)
	}

	void testQueryUnitLong() {
		def json = [unit: [eq: "eV"]]
		def params = [max: 10]

		def res = metaDataQueryService.query(json, params)

		assert res != null
		assert res.size() <= 10
	}

	void testQueryUnitInValueLong() {
		def json = [value: [ne: "10", unit: [like: "eV"]]]
		def params = [max: 10]

		def res = metaDataQueryService.query(json, params)

		assert res != null
		assert res.size() <= 10
	}

	void testEmptyQuery() {
		def json = []
		def params = [max: 10]

		def res = metaDataQueryService.query(json, params)

		log.debug("res: $res")
		shouldFail {}
		assert res != null
		assert res.size() <= 10
	}

	void testSimplestQuery() {
		def json = [name: "ms type"]
		def params = [max: 10]

		def res = metaDataQueryService.query(json, params)

		log.debug("res: $res")
		assert res != null
		assert res.size() <= 10
	}
}
