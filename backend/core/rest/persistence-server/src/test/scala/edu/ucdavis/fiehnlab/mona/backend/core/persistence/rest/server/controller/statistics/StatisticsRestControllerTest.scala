package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.statistics

import java.io.InputStreamReader
import com.jayway.restassured.RestAssured
import com.jayway.restassured.RestAssured._
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.domain.dao.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.domain.SpectrumResult
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.config.{EmbeddedRestServerConfig, TestConfig}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestContextManager
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.service.SpectrumPersistenceService
import edu.ucdavis.fiehnlab.mona.backend.core.domain.statistics.{StatisticsCompoundClasses, StatisticsGlobal, StatisticsMetaData, StatisticsSubmitter, StatisticsTag}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.AbstractSpringControllerTest

/**
  * Created by wohlgemuth on 3/8/16.
  */
@SpringBootTest(classes = Array(classOf[EmbeddedRestServerConfig], classOf[JWTAuthenticationConfig], classOf[TestConfig]), webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Array("test", "mona.persistence", "mona.persistence.init"))
class StatisticsRestControllerTest extends AbstractSpringControllerTest {

  @LocalServerPort
  private val port = 0

  @Autowired
  val spectrumRepository: SpectrumPersistenceService = null

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
        exampleRecords.foreach {
          x => spectrumRepository.save(new SpectrumResult(x.getId, x))
        }
      }

      "not update statistics as a non-admin" in {
        given().contentType("application/json; charset=UTF-8").log().all(true).when().post("/statistics/update").`then`().log().all(true).statusCode(401)
      }

      "update the statistics as an admin" in {
        authenticate().contentType("application/json; charset=UTF-8").log().all(true).when().post("/statistics/update").`then`().log().all(true).statusCode(200).extract()
      }

      "get metadata statistics" in {
        val result: Array[StatisticsMetaData] = given().contentType("application/json; charset=UTF-8").log().all(true).when().get("/statistics/metaData").`then`().log().all(true).statusCode(200).extract().as(classOf[Array[StatisticsMetaData]])
        assert(result.length == 574)
      }

      "get tag statistics" in {
        val result: Array[StatisticsTag] = given().contentType("application/json; charset=UTF-8").log().all(true).when().get("/tags").`then`().log().all(true).statusCode(200).extract().as(classOf[Array[StatisticsTag]])
        assert(result.length == 2)
      }

      "get library tag statistics" in {
        val result: Array[StatisticsTag] = given().contentType("application/json; charset=UTF-8").log().all(true).when().get("/tags/library").`then`().log().all(true).statusCode(200).extract().as(classOf[Array[StatisticsTag]])
        assert(result.length == 1)
      }

      "get global statistics" in {
        val result: StatisticsGlobal = given().contentType("application/json; charset=UTF-8").log().all(true).when().get("/statistics/global").`then`().log().all(true).statusCode(200).extract().as(classOf[StatisticsGlobal])

        assert(result.getSpectrumCount == 50)
        assert(result.getCompoundCount == 21)
        assert(result.getMetaDataCount == 574)
        assert(result.getMetaDataValueCount == 5254)
        assert(result.getTagCount == 2)
        assert(result.getTagValueCount == 100)
        assert(result.getSubmitterCount == 4)
      }

      "get compound class statistics" in {
        val result: Array[StatisticsCompoundClasses] = given().contentType("application/json; charset=UTF-8").log().all(true).when().get("/statistics/compoundClasses").`then`().log().all(true).statusCode(200).extract().as(classOf[Array[StatisticsCompoundClasses]])

        val organicCompounds = result.filter(_.getName == "Organic compounds")
        assert(organicCompounds.nonEmpty)
        assert(organicCompounds.head.getSpectrumCount == 50)
        assert(organicCompounds.head.getCompoundCount == 21)
      }

      "get submitter statistics" in {
        val result: Array[StatisticsSubmitter] = given().contentType("application/json; charset=UTF-8").log().all(true).when().get("/statistics/submitters").`then`().log().all(true).statusCode(200).extract().as(classOf[Array[StatisticsSubmitter]])
        assert(result.length == 4)
      }
    }
  }
}
