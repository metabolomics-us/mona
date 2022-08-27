/*
package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.metadata

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper

import java.io.InputStreamReader
import com.jayway.restassured.RestAssured
import com.jayway.restassured.RestAssured._
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.dao.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.MonaMapper
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.SpectrumResult
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.views.SearchTableRepository
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.mat.MaterializedViewRepository
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.statistics.StatisticsMetaData
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.SpectrumResultRepository
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.config.{EmbeddedRestServerConfig, TestConfig}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.AbstractSpringControllerTest
import org.scalatest.concurrent.Eventually
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.jdk.CollectionConverters._

/**
  * Created by wohlgemuth on 3/8/16.
  */
@SpringBootTest(classes = Array(classOf[EmbeddedRestServerConfig], classOf[JWTAuthenticationConfig], classOf[TestConfig]), webEnvironment = WebEnvironment.DEFINED_PORT)
@ActiveProfiles(Array("test", "mona.persistence", "mona.persistence.init"))
class MetaDataRestControllerTest extends AbstractSpringControllerTest with Eventually{

  @LocalServerPort
  private val port = 0

  @Autowired
  val spectrumRepository: SpectrumResultRepository = null

  @Autowired
  val searchTableRepository: SearchTableRepository = null

  @Autowired
  val matRepository: MaterializedViewRepository = null

  @Autowired
  val monaMapper: ObjectMapper = {
    MonaMapper.create
  }

  // required for spring and scala tests
  new TestContextManager(this.getClass).prepareTestInstance(this)

  "we will be connecting to the REST controller" when {
    RestAssured.baseURI = s"http://localhost:$port/rest"

    "when connected we should be able to" should {
      "clear repository and add data" in {
        spectrumRepository.deleteAll()
        assert(spectrumRepository.count() == 0)

        val exampleRecords: Array[Spectrum] = monaMapper.readValue(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")), new TypeReference[Array[Spectrum]] {})
        assert(exampleRecords.length == 59)

        exampleRecords.foreach { x => spectrumRepository.save(new SpectrumResult(x.getId, x)) }
      }

      s"we should be able to create our materialized view" in {

        eventually(timeout(180 seconds)) {
          matRepository.refreshSearchTable()
          logger.info("sleep...")
          assert(searchTableRepository.count() == 59610)
        }

      }

      "we should be able to generate statistics" in {
        authenticate().contentType("application/json; charset=UTF-8").when().post("/statistics/update").`then`().log().all(true).statusCode(200).extract()
      }

      "we should be able to query all meta data names from the service" in {
        val result = given().contentType("application/json; charset=UTF-8").when().get("/metaData/names").`then`().log().all(true).statusCode(200).extract().body().as(classOf[Array[StatisticsMetaData]])
        assert(result.length == 240)
      }

      "we should be able to search for metadata names" in {
        val result = given().contentType("application/json; charset=UTF-8").when().get("/metaData/names?search=inst").`then`().log().all(true).statusCode(200).extract().body().as(classOf[Array[StatisticsMetaData]])
        assert(result.length == 7)
      }

      "we should be able to query all the meta data values for a specific name" in {
        val result = given().contentType("application/json; charset=UTF-8").when().get("/metaData/values?name=authors").`then`().log().all(true).statusCode(200).extract().body().as(classOf[StatisticsMetaData])

        assert(result.getName == "authors")
        assert(result.getMetaDataValueCount.size() == 1)
        assert(result.getMetaDataValueCount.asScala.head.getValue == "Mark Earll, Stephan Beisken, EMBL-EBI")
        assert(result.getMetaDataValueCount.asScala.head.getCount == 58)
      }

      "we should be able to query all the meta data values for a specific name that contains spaces" in {
        val result = given().contentType("application/json; charset=UTF-8").when().get("/metaData/values?name=ms level").`then`().log().all(true).statusCode(200).extract().body().as(classOf[StatisticsMetaData])

        assert(result.getName == "ms level")
        assert(result.getMetaDataValueCount.size() == 1)
        assert(result.getMetaDataValueCount.asScala.head.getValue == "MS2")
        assert(result.getMetaDataValueCount.asScala.head.getCount == 59)
      }

      "we should be able to query all the meta data values for a specific name that special characters" in {
        val result = given().contentType("application/json; charset=UTF-8").when().get("/metaData/values?name=precursor m/z").`then`().log().all(true).statusCode(200).extract().body().as(classOf[StatisticsMetaData])

        assert(result.getName == "precursor m/z")
        assert(result.getMetaDataValueCount.size() == 56)
      }

      "we should be able to search for metadata values" in {
        val result = given().contentType("application/json; charset=UTF-8").when().get("/metaData/values?name=authors&search=Mark").`then`().log().all(true).statusCode(200).extract().body().as(classOf[StatisticsMetaData])
        assert(result.getMetaDataValueCount.size() == 1)
      }
    }
  }
}
*/
