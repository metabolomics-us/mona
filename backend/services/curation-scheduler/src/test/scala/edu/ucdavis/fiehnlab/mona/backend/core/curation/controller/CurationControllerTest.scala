package edu.ucdavis.fiehnlab.mona.backend.core.curation.controller

import java.io.{InputStreamReader, StringWriter}
import com.jayway.restassured.RestAssured
import com.jayway.restassured.RestAssured._
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus.ReceivedEventCounter
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.config.{MonaNotificationBusCounterConfiguration, Notification}
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.service.PostgresLoginService
import edu.ucdavis.fiehnlab.mona.backend.core.curation.CurationScheduler
import edu.ucdavis.fiehnlab.mona.backend.core.domain.SpectrumResult
import edu.ucdavis.fiehnlab.mona.backend.core.domain.dao.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.{JSONDomainReader, MonaMapper}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.config.PostgresqlConfiguration
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.SpectrumResultRepository
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.mat.MaterializedViewRepository
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.views.SearchTableRepository
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.AbstractSpringControllerTest
import org.scalatest.concurrent.Eventually
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.{ActiveProfiles, TestContextManager}
import org.springframework.web.bind.annotation.GetMapping

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.jdk.CollectionConverters._
import scala.language.postfixOps


/**
  * Created by wohlg on 4/13/2016.
  */

@SpringBootTest(classes = Array(classOf[CurationScheduler], classOf[MonaNotificationBusCounterConfiguration]), webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Array("test", "mona.persistence", "mona.persistence.init"))
class CurationControllerTest extends AbstractSpringControllerTest with Eventually {

  @LocalServerPort
  private val port = 0

  @Autowired
  val notificationCounter: ReceivedEventCounter[Notification] = null

  @Autowired
  val spectrumResultRepository: SpectrumResultRepository = null

  @Autowired
  val matRepository: MaterializedViewRepository = null

  @Autowired
  val searchTableRepository: SearchTableRepository = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "CurationControllerTest" should {

    RestAssured.baseURI = s"http://localhost:$port/rest"

    val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")))

    "load some data" in {
      spectrumResultRepository.deleteAll()
      exampleRecords.foreach(x => spectrumResultRepository.save(new SpectrumResult(x.getId, x)))
    }

    s"we should be able to refresh our materialized view" in {
      eventually(timeout(180 seconds)) {
        matRepository.refreshSearchTable()
        logger.info("sleep...")
        assert(searchTableRepository.count() == 59610)
      }
    }

    "these must all fail, since we require to be logged in " must {
      "curateByQuery" in {
        given().contentType("application/json; charset=UTF-8").when().get("/curation?query=metadataName==\'ion mode\' and metadataValue==\'negative\'").`then`().statusCode(401)
      }

      "curateById" in {
        given().contentType("application/json; charset=UTF-8").when().get("/curation/252").`then`().statusCode(401)
      }

      "curateAll" in {
        given().contentType("application/json; charset=UTF-8").when().get("/curation").`then`().statusCode(401)
      }
    }

    "these must all fail, since we require to be an admin " must {
      "curateByQuery" in {
        authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").when().get("/curation?query=metadataName==\'ion mode\' and metadataValue==\'negative\'").`then`().statusCode(403)
      }

      "curateById" in {
        authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").when().get("/curation/252").`then`().statusCode(403)
      }

      "curateAll" in {
        authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").when().get("/curation").`then`().statusCode(403)
      }
    }

    "these must all pass, since we are logged in " must {
      "curateByQuery" in {
        val count: Long = notificationCounter.getEventCount
        val result = authenticate().contentType("application/json; charset=UTF-8").when().get("/curation?query=metadataName==\'ion mode\' and metadataValue==\'negative\'").`then`().statusCode(200).extract().body().as(classOf[CurationJobScheduled])

        assert(result.count == 25)

        eventually(timeout(80 seconds)) {
          assert(notificationCounter.getEventCount - count == result.count)
        }
      }

      "curateByIdWithWrongIdShouldReturn404" in {
        authenticate().contentType("application/json; charset=UTF-8").when().get("/curation/IDONOTEXIST").`then`().statusCode(404)
      }

      "curateById" in {
        val count: Long = notificationCounter.getEventCount
        val spec: SpectrumResult = spectrumResultRepository.findAll().iterator().next()
        val result: CurationJobScheduled = authenticate().contentType("application/json; charset=UTF-8").when().get(s"/curation/${spec.getMonaId}").`then`().statusCode(200).extract().body().as(classOf[CurationJobScheduled])

        assert(result.count == 1)

        eventually(timeout(80 seconds)) {
          assert(notificationCounter.getEventCount - count == result.count)
        }
      }

      "curateAll" in {
        val count: Long = notificationCounter.getEventCount
        val result: CurationJobScheduled = authenticate().contentType("application/json; charset=UTF-8").when().get("/curation").`then`().statusCode(200).extract().body().as(classOf[CurationJobScheduled])

        assert(result.count == exampleRecords.length)

        eventually(timeout(80 seconds)) {
          assert(notificationCounter.getEventCount - count == result.count)
        }
      }

      "curateSpectrum" in {
        val mapper = MonaMapper.create
        val writer = new StringWriter()
        mapper.writeValue(writer, exampleRecords.head)

        val content = writer.toString
        val result: Spectrum = given().contentType("application/json; charset=UTF-8").body(content).when().post("/curation").`then`().statusCode(200).extract().body().as(classOf[Spectrum])

        assert(result.getScore != null)
        assert(result.getScore.getImpacts.asScala.nonEmpty)

        assert(exampleRecords.head.getLastCurated == null)
        assert(result.getLastCurated != null)
      }
    }
  }
}

@Configuration
class TestConfig {

  /**
    * the service which actually does the login for us
    *
    * @return
    */
  @Bean
  def loginService: LoginService = new PostgresLoginService
}
