package edu.ucdavis.fiehnlab.mona.backend.services.repository.listener

import java.io.{File, InputStreamReader}

import com.fasterxml.jackson.databind.ObjectMapper
import com.jayway.restassured.RestAssured
import com.jayway.restassured.RestAssured._
import com.jayway.restassured.config.ObjectMapperConfig
import com.jayway.restassured.mapper.factory.Jackson2ObjectMapperFactory
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.event.Event
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.{JSONDomainReader, MonaMapper}
import edu.ucdavis.fiehnlab.mona.backend.services.repository.WebRepository
import edu.ucdavis.fiehnlab.mona.backend.services.repository.utility.FindDirectory
import org.apache.commons.io.FileUtils
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.embedded.LocalServerPort
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner

import scala.util.Properties


/**
  * Created by wohlg_000 on 5/18/2016.
  */
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[WebRepository]), webEnvironment = WebEnvironment.RANDOM_PORT, properties = Array("eureka.client.enabled:false"))
class WebRepositoryListenerTest extends WordSpec with LazyLogging {

  @LocalServerPort
  private val port = 0

  @Autowired
  val repositoryListener: RepositoryListener = null

  @Autowired
  val locator: FindDirectory = null

  val reader: JSONDomainReader[Spectrum] = JSONDomainReader.create[Spectrum]

  val keepRunning: Boolean = Properties.envOrElse("keep.server.running", "false").toBoolean

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "RepositoryListenerTest" must {

    RestAssured.config = RestAssured.config().objectMapperConfig(ObjectMapperConfig.objectMapperConfig().jackson2ObjectMapperFactory(new Jackson2ObjectMapperFactory {
      override def create(aClass: Class[_], s: String): ObjectMapper = MonaMapper.create
    }))

    RestAssured.baseURI = s"http://localhost:$port/"

    val input = new InputStreamReader(getClass.getResourceAsStream("/monaRecord.json"))
    val spectrum: Spectrum = reader.read(input)

    "be able to receive data and " should {
      "create a file on an add event" in {
        repositoryListener.received(Event(spectrum, eventType = Event.ADD))
      }

      "create a file on an update event" in {
        repositoryListener.received(Event(spectrum, eventType = Event.UPDATE))
      }

      "delete a file on a delete event" in {
        repositoryListener.received(Event(spectrum, eventType = Event.ADD))
        repositoryListener.received(Event(spectrum, eventType = Event.DELETE))
      }
    }

    "be able to expose data as endpoint" should {

      "be able to access /repository and browse it " in {
        given().contentType("application/json; charset=UTF-8").when().log.all(true).get("/repository").`then`().statusCode(200)
      }

      "be able to access our spectra file" in {
        repositoryListener.received(Event(spectrum, eventType = Event.ADD))
        val spectra = given().contentType("application/json; charset=UTF-8").when().get(s"/repository/Boise_State_University/QASFUMOKHFSJGL-LAFRSMQTSA-N/splash10-0bt9-0910000000-9c8c58860a0fadd33800/252.json").`then`().statusCode(200).extract().as(classOf[Spectrum])

        assert(spectra.id == "252")
      }

      "be able to delete our spectra file" in {
        repositoryListener.received(Event(spectrum, eventType = Event.DELETE))
        given().contentType("application/json; charset=UTF-8").when().get(s"/repository/Boise_State_University/QASFUMOKHFSJGL-LAFRSMQTSA-N/splash10-0bt9-0910000000-9c8c58860a0fadd33800/252.json").`then`().statusCode(404)
      }

      "if specified the server should stay online, this can be done using the env variable 'keep.server.running=true' " in {
        if (keepRunning) {
          while (keepRunning) {
            logger.warn("waiting forever till you kill me!")
            Thread.sleep(300000); // Every 5 minutes
          }
        }
      }

      "clear the git reposisoty data" in {
        val repositoryDirectory: File = new File(locator.dir)

        if (repositoryDirectory.exists()) {
          FileUtils.deleteDirectory(repositoryDirectory)
        }

        assert(!repositoryDirectory.exists())
      }
    }
  }
}
