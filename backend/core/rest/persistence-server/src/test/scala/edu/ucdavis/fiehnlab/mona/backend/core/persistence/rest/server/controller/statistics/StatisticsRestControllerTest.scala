package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.statistics

import java.io.InputStreamReader

import com.fasterxml.jackson.databind.ObjectMapper
import com.jayway.restassured.RestAssured
import com.jayway.restassured.RestAssured._
import com.jayway.restassured.config.ObjectMapperConfig
import com.jayway.restassured.mapper.factory.Jackson2ObjectMapperFactory
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.domain.HelperTypes.WrappedString
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.{JSONDomainReader, MonaMapper}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.ISpectrumMongoRepositoryCustom
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.config.{EmbeddedRestServerConfig, TestConfig}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.AbstractSpringControllerTest
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.config.StatisticsRepositoryConfig
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.types.{MetaDataStatistics, TagStatistics}
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.http.MediaType
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
  * Created by wohlgemuth on 3/8/16.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[EmbeddedRestServerConfig], classOf[JWTAuthenticationConfig], classOf[TestConfig]))
class StatisticsRestControllerTest extends AbstractSpringControllerTest {

  @Autowired
  val spectrumRepository: ISpectrumMongoRepositoryCustom = null

  @Autowired
  val mongoOperations: MongoOperations = null

  // required for spring and scala tests
  new TestContextManager(this.getClass).prepareTestInstance(this)


  "we will be connecting to the REST controller" when {

    RestAssured.config = RestAssured.config().objectMapperConfig(ObjectMapperConfig.objectMapperConfig().jackson2ObjectMapperFactory(new Jackson2ObjectMapperFactory {
      override def create(aClass: Class[_], s: String): ObjectMapper = MonaMapper.create
    }))


    RestAssured.baseURI = s"http://localhost:$port/rest"

    "when connected we should be able to" should {

      "upload data" in {
        spectrumRepository.deleteAll()

        //58 spectra for us to work with
        val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")))
        assert(exampleRecords.length == 58)

        //save each record
        exampleRecords.foreach { x => spectrumRepository.save(x) }
      }

      "not update statistics as a non-admin" in {
        given().contentType("application/json; charset=UTF-8").log().all(true).when().post("/statistics/update").then().log().all(true).statusCode(401)
      }

      "update the statistics as an admin" in {
        val result: StatisticsSummary = authenticate().contentType("application/json; charset=UTF-8").log().all(true).when().post("/statistics/update").then().log().all(true).statusCode(200).extract().as(classOf[StatisticsSummary])

        assert(result.metaDataCount == 44)
        assert(result.tagsCount == 3)
      }

      "get metadata statistics" in {
        val result: Array[MetaDataStatistics] = given().contentType("application/json; charset=UTF-8").log().all(true).when().get("/statistics/metaData").then().log().all(true).statusCode(200).extract().as(classOf[Array[MetaDataStatistics]])
        assert(result.length == 44)
      }

      "get tag statistics" in {
        val result: Array[TagStatistics] = given().contentType("application/json; charset=UTF-8").log().all(true).when().get("/statistics/tags").then().log().all(true).statusCode(200).extract().as(classOf[Array[TagStatistics]])
        assert(result.length == 3)
      }
    }
  }
}
