package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.metadata

import java.io.InputStreamReader

import com.fasterxml.jackson.databind.ObjectMapper
import com.jayway.restassured.RestAssured
import com.jayway.restassured.RestAssured._
import com.jayway.restassured.config.ObjectMapperConfig
import com.jayway.restassured.mapper.factory.Jackson2ObjectMapperFactory
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.{JSONDomainReader, MonaMapper}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.ISpectrumMongoRepositoryCustom
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.config.{EmbeddedRestServerConfig, TestConfig}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.AbstractSpringControllerTest
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.types.{MetaDataStatistics, MetaDataStatisticsSummary}
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
  * Created by wohlgemuth on 3/8/16.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[EmbeddedRestServerConfig], classOf[JWTAuthenticationConfig], classOf[TestConfig]))
class MetaDataRestControllerTest extends AbstractSpringControllerTest {

  @Autowired
  val spectrumRepository: ISpectrumMongoRepositoryCustom = null

  // required for spring and scala tests
  new TestContextManager(this.getClass).prepareTestInstance(this)


  "we will be connecting to the REST controller" when {

    RestAssured.config = RestAssured.config().objectMapperConfig(ObjectMapperConfig.objectMapperConfig().jackson2ObjectMapperFactory(new Jackson2ObjectMapperFactory {
      override def create(aClass: Class[_], s: String): ObjectMapper = MonaMapper.create
    }))

    RestAssured.baseURI = s"http://localhost:$port/rest"

    "when connected we should be able to" should {

      "clear repository and add data" in {
        spectrumRepository.deleteAll()
        assert(spectrumRepository.count() == 0)

        //58 spectra for us to work with
        val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")))
        assert(exampleRecords.length == 58)

        //save each record
        exampleRecords.foreach { x => spectrumRepository.save(x) }
      }

      "we should be able to generate statistics" in {
        authenticate().contentType("application/json; charset=UTF-8").log().all().when().post("/statistics/update").then().log().all(true).statusCode(200).extract()
      }

      "we should be able to query all meta data names from the service" in {
        val result = given().contentType("application/json; charset=UTF-8").log().all().when().get("/metaData/names").then().log().all(true).statusCode(200).extract().body().as(classOf[Array[MetaDataStatisticsSummary]])
        assert(result.length == 44)
      }

      "we should be able to search for metadata names" in {
        val result = given().contentType("application/json; charset=UTF-8").log().all().when().get("/metaData/names?search=inst").then().log().all(true).statusCode(200).extract().body().as(classOf[Array[MetaDataStatisticsSummary]])
        assert(result.length == 2)
      }

      "we should be able to query all the meta data values for a specific name" in {
        val result = given().contentType("application/json; charset=UTF-8").log().all().when().get("/metaData/values?name=authors").then().log().all(true).statusCode(200).extract().body().as(classOf[MetaDataStatistics])

        assert(result.name == "authors")
        assert(result.values.length == 1)
        assert(result.values.head.value == "Mark Earll, Stephan Beisken, EMBL-EBI")
        assert(result.values.head.count == 58)
      }

      "we should be able to query all the meta data values for a specific name that contains spaces" in {
        val result = given().contentType("application/json; charset=UTF-8").log().all().when().get("/metaData/values?name=ms level").then().log().all(true).statusCode(200).extract().body().as(classOf[MetaDataStatistics])

        assert(result.name == "ms level")
        assert(result.values.length == 1)
        assert(result.values.head.value == "MS2")
        assert(result.values.head.count == 58)
      }

      "we should be able to query all the meta data values for a specific name that special characters" in {
        val result = given().contentType("application/json; charset=UTF-8").log().all().when().get("/metaData/values?name=precursor m/z").then().log().all(true).statusCode(200).extract().body().as(classOf[MetaDataStatistics])

        assert(result.name == "precursor m/z")
        assert(result.values.length == 55)
      }

      "we should be able to search for metadata values" in {
        val result = given().contentType("application/json; charset=UTF-8").log().all().when().get("/metaData/values?name=authors&search=Mark").then().log().all(true).statusCode(200).extract().body().as(classOf[MetaDataStatistics])
        assert(result.values.length == 1)
      }
    }
  }
}
