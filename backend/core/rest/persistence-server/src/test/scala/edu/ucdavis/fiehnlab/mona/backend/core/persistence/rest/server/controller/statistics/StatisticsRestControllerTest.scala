package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.statistics

import java.io.InputStreamReader

import com.jayway.restassured.RestAssured
import com.jayway.restassured.RestAssured._
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.ISpectrumMongoRepositoryCustom
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.config.{EmbeddedRestServerConfig, TestConfig}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.AbstractSpringControllerTest
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.types._
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.embedded.LocalServerPort
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner

/**
  * Created by wohlgemuth on 3/8/16.
  */
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[EmbeddedRestServerConfig], classOf[JWTAuthenticationConfig], classOf[TestConfig]), webEnvironment = WebEnvironment.DEFINED_PORT)
class StatisticsRestControllerTest extends AbstractSpringControllerTest {

  @LocalServerPort
  private val port = 0

  @Autowired
  val spectrumRepository: ISpectrumMongoRepositoryCustom = null

  @Autowired
  val mongoOperations: MongoOperations = null

  // required for spring and scala tests
  new TestContextManager(this.getClass).prepareTestInstance(this)


  "we will be connecting to the REST controller" when {
    RestAssured.baseURI = s"http://localhost:$port/rest"

    "when connected we should be able to" should {

      "upload data" in {
        spectrumRepository.deleteAll()

        //50 spectra for us to work with
        val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/curatedRecords.json")))
        assert(exampleRecords.length == 50)

        //save each record
        exampleRecords.foreach { x => spectrumRepository.save(x) }
      }

      "not update statistics as a non-admin" in {
        given().contentType("application/json; charset=UTF-8").log().all(true).when().post("/statistics/update").`then`().log().all(true).statusCode(401)
      }

      "update the statistics as an admin" in {
        authenticate().contentType("application/json; charset=UTF-8").log().all(true).when().post("/statistics/update").`then`().log().all(true).statusCode(200).extract()
      }

      "get metadata statistics" in {
        val result: Array[MetaDataStatistics] = given().contentType("application/json; charset=UTF-8").log().all(true).when().get("/statistics/metaData").`then`().log().all(true).statusCode(200).extract().as(classOf[Array[MetaDataStatistics]])
        assert(result.length == 21)
      }

      "get tag statistics" in {
        val result: Array[TagStatistics] = given().contentType("application/json; charset=UTF-8").log().all(true).when().get("/tags").`then`().log().all(true).statusCode(200).extract().as(classOf[Array[TagStatistics]])
        assert(result.length == 2)
      }

      "get library tag statistics" in {
        val result: Array[TagStatistics] = given().contentType("application/json; charset=UTF-8").log().all(true).when().get("/tags/library").`then`().log().all(true).statusCode(200).extract().as(classOf[Array[TagStatistics]])
        assert(result.length == 1)
      }

      "get global statistics" in {
        val result: GlobalStatistics = given().contentType("application/json; charset=UTF-8").log().all(true).when().get("/statistics/global").`then`().log().all(true).statusCode(200).extract().as(classOf[GlobalStatistics])

        assert(result.spectrumCount == 50)
        assert(result.compoundCount == 21)
        assert(result.metaDataCount == 21)
        assert(result.metaDataValueCount == 1050)
        assert(result.tagCount == 2)
        assert(result.tagValueCount == 100)
        assert(result.submitterCount == 1)
      }

      "get compound class statistics" in {
        val result: Array[CompoundClassStatistics] = given().contentType("application/json; charset=UTF-8").log().all(true).when().get("/statistics/compoundClasses").`then`().log().all(true).statusCode(200).extract().as(classOf[Array[CompoundClassStatistics]])

        val organicCompounds = result.filter(_.name == "Organic compounds")
        assert(organicCompounds.nonEmpty)
        assert(organicCompounds.head.spectrumCount == 50)
        assert(organicCompounds.head.compoundCount == 21)
      }

      "get submitter statistics" in {
        val result: Array[SubmitterStatistics] = given().contentType("application/json; charset=UTF-8").log().all(true).when().get("/statistics/submitters").`then`().log().all(true).statusCode(200).extract().as(classOf[Array[SubmitterStatistics]])
        assert(result.length == 1)
      }
    }
  }
}
