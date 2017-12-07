package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.metadata

import java.io.InputStreamReader

import com.jayway.restassured.RestAssured
import com.jayway.restassured.RestAssured._
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.ISpectrumMongoRepositoryCustom
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.config.{EmbeddedRestServerConfig, TestConfig}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.AbstractSpringControllerTest
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.types.{MetaDataStatistics, MetaDataStatisticsSummary}
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.embedded.LocalServerPort
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner

/**
  * Created by wohlgemuth on 3/8/16.
  */
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[EmbeddedRestServerConfig], classOf[JWTAuthenticationConfig], classOf[TestConfig]), webEnvironment = WebEnvironment.DEFINED_PORT)
class MetaDataRestControllerTest extends AbstractSpringControllerTest {

  @LocalServerPort
  private val port = 0

  @Autowired
  val spectrumRepository: ISpectrumMongoRepositoryCustom = null

  // required for spring and scala tests
  new TestContextManager(this.getClass).prepareTestInstance(this)

  "we will be connecting to the REST controller" when {
    RestAssured.baseURI = s"http://localhost:$port/rest"

    "when connected we should be able to" should {
      "clear repository and add data" in {
        spectrumRepository.deleteAll()
        assert(spectrumRepository.count() == 0)

        val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")))
        assert(exampleRecords.length == 58)

        exampleRecords.foreach { x => spectrumRepository.save(x) }
      }

      "we should be able to generate statistics" in {
        authenticate().contentType("application/json; charset=UTF-8").log().all().when().post("/statistics/update").`then`().log().all(true).statusCode(200).extract()
      }

      "we should be able to query all meta data names from the service" in {
        val result = given().contentType("application/json; charset=UTF-8").log().all().when().get("/metaData/names").`then`().log().all(true).statusCode(200).extract().body().as(classOf[Array[MetaDataStatisticsSummary]])
        assert(result.length == 44)
      }

      "we should be able to search for metadata names" in {
        val result = given().contentType("application/json; charset=UTF-8").log().all().when().get("/metaData/names?search=inst").`then`().log().all(true).statusCode(200).extract().body().as(classOf[Array[MetaDataStatisticsSummary]])
        assert(result.length == 2)
      }

      "we should be able to query all the meta data values for a specific name" in {
        val result = given().contentType("application/json; charset=UTF-8").log().all().when().get("/metaData/values?name=authors").`then`().log().all(true).statusCode(200).extract().body().as(classOf[MetaDataStatistics])

        assert(result.name == "authors")
        assert(result.values.length == 1)
        assert(result.values.head.value == "Mark Earll, Stephan Beisken, EMBL-EBI")
        assert(result.values.head.count == 58)
      }

      "we should be able to query all the meta data values for a specific name that contains spaces" in {
        val result = given().contentType("application/json; charset=UTF-8").log().all().when().get("/metaData/values?name=ms level").`then`().log().all(true).statusCode(200).extract().body().as(classOf[MetaDataStatistics])

        assert(result.name == "ms level")
        assert(result.values.length == 1)
        assert(result.values.head.value == "MS2")
        assert(result.values.head.count == 58)
      }

      "we should be able to query all the meta data values for a specific name that special characters" in {
        val result = given().contentType("application/json; charset=UTF-8").log().all().when().get("/metaData/values?name=precursor m/z").`then`().log().all(true).statusCode(200).extract().body().as(classOf[MetaDataStatistics])

        assert(result.name == "precursor m/z")
        assert(result.values.length == 55)
      }

      "we should be able to search for metadata values" in {
        val result = given().contentType("application/json; charset=UTF-8").log().all().when().get("/metaData/values?name=authors&search=Mark").`then`().log().all(true).statusCode(200).extract().body().as(classOf[MetaDataStatistics])
        assert(result.values.length == 1)
      }
    }
  }
}
