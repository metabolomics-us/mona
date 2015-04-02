package moa.server.query

import com.github.fge.jackson.JsonLoader
import com.github.fge.jsonschema.core.report.ProcessingReport
import com.github.fge.jsonschema.main.JsonSchema
import com.github.fge.jsonschema.main.JsonSchemaFactory
import groovy.json.JsonBuilder
import moa.MetaDataValue
import org.apache.log4j.Logger
import org.junit.Before
/**
 * Created by diego on 3/31/15.
 */
class MetaDataQueryServiceTest extends GroovyTestCase {
	private Logger log = Logger.getLogger(this.class.name)

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
		assert res.size() == 10
		assert (res['stringValue'] as HashSet<String>).contains("MS2")
		assert (res['stringValue'] as HashSet<String>).size() == 1
	}

	void testQueryLongJson() {
		def json = [name:["like":"ms type"], value:["eq":"MS2"]]
		def params = [max:10]

		def res = metaDataQueryService.query(json, params)

		assert res != null
		assert res.size() == 10
		assert (res['stringValue'] as HashSet<String>).contains("MS2")
		assert (res['stringValue'] as HashSet<String>).size() == 1
	}

	void testQueryJsonBetween() {
		def json = [value: [between: ["0", "1000"]]]
		def params = [max: 10]

		def res = metaDataQueryService.query(json, params)

		assert res != null
		assert res.size() == 10
	}

	void testQueryCategoryShort() {
		def json = [category: "spectral properties"]
		def params = [max: 10]

		def res = metaDataQueryService.query(json, params)

		assert res != null
		assert res.size() == 10
		assert "143".equals(res[0].value)
		assert "num peaks".equals(res[0].metaData.name)
	}

	void testQueryCategoryLong() {
		def json = [category: [eq: "spectral properties"]]
		def params = [max: 10]

		def res = metaDataQueryService.query(json, params)

		assert res != null
		assert res.size() == 10
		assert "143".equals(res[0].value)
		assert "num peaks".equals(res[0].metaData.name)
	}

	void testQueryUnitLong() {
		def json = [unit: [eq: "eV"]]
		def params = [max: 10]

		def res = metaDataQueryService.query(json, params)

		assert res != null
		assert res.size() == 0 // units not implemented yet, change to 10 after implementation
	}

	void testQueryUnitShort() {
		def json = [unit: "eV"]
		def params = [max: 10]

		def res = metaDataQueryService.query(json, params)

		assert res != null
		assert res.size() == 0 // units not implemented yet, change to 10 after implementation
	}

	void testQueryUnitInValueLong() {
		def json = [value: [ne: "10", unit: "eV"]]
		def params = [max: 10]

		def res = metaDataQueryService.query(json, params)

		assert res != null
		assert res.size() == 0 // units not implemented yet, change to 10 after implementation
	}

	void testEmptyQuery() {
		def json = [:]
		def params = [max: 10]

		def res = metaDataQueryService.query(json, params)

		log.debug("res: $res")
		shouldFail {}
		assert res != null
		assert res.size() == 10
	}

	void testSimplestQuery() {
		def json = [name: "ms type"]
		def params = [max: 10]

		def res = metaDataQueryService.query(json, params)

		log.debug("res: $res")
		assert res != null
		assert res.size() == 10
	}

	void testValidCheck() {
		def jsonObj = [name: ["like": "ms type"], value: ["eq": "MS2"]]
		def jsonBadObj = [value: ["eq": "MS2"]]

		/**
		 * checks the json object against a schema
		 * @param jsonObj map containing the json object
		 * @return true if the json object is valid according to the schema, false other way.
		 */
		def jsonString = new JsonBuilder(jsonObj).toString()
		def badJsonString = new JsonBuilder(jsonBadObj).toString()

		def qsFile = new File("schemas/QuerySchema.json")

		final JsonSchemaFactory factory = JsonSchemaFactory.byDefault()
		final JsonSchema schema = factory.getJsonSchema(qsFile.toURI().toString().concat("#/definitions/metadata/items/0"))

		ProcessingReport report
		report = schema.validate(JsonLoader.fromString(jsonString))

		assert report.success

		ProcessingReport report2
		report2 = schema.validate(JsonLoader.fromString(badJsonString))

		assert report2.success == false
		assert report2[0].message.contains("property \"value\"")
		assert report2[0].message.contains("requires [\"name\"]")
		assert report2[0].message.contains("missing: [\"name\"]")

//			return false

	}
}
