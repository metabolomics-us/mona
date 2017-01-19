package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.controller

import com.jayway.restassured.RestAssured._
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.config.{BusConfig, MonaEventBusConfiguration, MonaNotificationBusConfiguration}
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.service.MongoLoginService
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.types.TokenSecret
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.AbstractGenericRESTControllerTest
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.config.WebHookSecurity
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.repository.{WebHookRepository, WebHookResultRepository}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.types.{WebHook, WebHookResult}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.service.persistence.SpectrumPersistenceService
import org.junit.runner.RunWith
import org.scalatest.ShouldMatchers
import org.scalatest.concurrent.Eventually
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.{SpringApplicationConfiguration, WebIntegrationTest}
import org.springframework.context.annotation.{Bean, Configuration, Import}
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import scala.concurrent.duration._

@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[TestConfig]))
class WebhookControllerTest extends AbstractGenericRESTControllerTest[WebHook]("/webhooks") with Eventually with ShouldMatchers with LazyLogging{

  @Autowired
  val webHookRepository: WebHookRepository = null

  @Autowired
  val webHookResultRepository: WebHookResultRepository = null

  @Autowired
  val spectrumPersistenceService: SpectrumPersistenceService = null

  //required for spring and scala tes
  new TestContextManager(this.getClass()).prepareTestInstance(this)

  /**
    * object to use for gets
    *
    * @return
    */
  override def getValue: WebHook = WebHook("my test hook", s"http://localhost:${port}/info", "just for testing")

  /**
    * returns an id for us for testing
    *
    * @return
    */
  override def getId: String = getValue.name

  "a webhook controller" must {

    "triggering requires authentication" in {
      given().contentType("application/json; charset=UTF-8").body(getValue).when().post(s"/webhooks").then().statusCode(401)
    }

    "be able to trigger the registered external urls" in {
      webHookRepository.deleteAll()
      authenticate().contentType("application/json; charset=UTF-8").body(getValue).when().post(s"/webhooks").then().statusCode(200)

      val result: Array[WebHookResult] = authenticate().log().all(true).contentType("application/json; charset=UTF-8").when().post(s"/webhooks/trigger/${getId}/add").then().log().all(true).statusCode(200).extract().body().as(classOf[Array[WebHookResult]])

      assert(result.size == 1)

      result.foreach { x => assert(x.success) }

    }


    "be able to trigger the registered external urls and not crash in case they cant be reached" in {
      webHookRepository.deleteAll()
      authenticate().contentType("application/json; charset=UTF-8").body(WebHook("i can't be reached", "http://localhost:21234/rest", "none provided")).when().post(s"/webhooks").then().statusCode(200)

      val result: Array[WebHookResult] = authenticate().log().all(true).contentType("application/json; charset=UTF-8").when().post(s"/webhooks/trigger/${getId}/add").then().log().all(true).statusCode(200).extract().body().as(classOf[Array[WebHookResult]])

      assert(result.size == 1)

      result.foreach { x =>
        assert(!x.success)
        assert(x.error != "")

      }
    }


    "be able to trigger the registered external urls and not crash in case they don't exist" in {
      webHookRepository.deleteAll()
      authenticate().contentType("application/json; charset=UTF-8").body(WebHook("i dont exist", s"http://localhost:${port}/info/id/", "this tosses a 401")).when().post(s"/webhooks").then().statusCode(200)

      val result: Array[WebHookResult] = authenticate().log().all(true).contentType("application/json; charset=UTF-8").when().post(s"/webhooks/trigger/${getId}/add").then().log().all(true).statusCode(200).extract().body().as(classOf[Array[WebHookResult]])

      assert(result.size == 1)

      result.foreach { x =>
        assert(!x.success)
        assert(x.error != "")

      }
    }
    "be able to pull a copy from the remote server" ignore {

      "require authentication" in {
        given().body(getValue).when().post(s"/webhooks/pull").then().statusCode(401)
      }


      "require admin authentication" in {
        authenticate("test", "test-secret").body(getValue).when().post(s"/webhooks/pull").then().statusCode(403)
      }

      "be able to download a specific query from the remote, with a small amount of results" in {
        spectrumPersistenceService.deleteAll()
        eventually(timeout(50 seconds), interval(1000 millis)) {
          logger.info(s"local db is now: ${spectrumPersistenceService.count()} spectra")
          assert(spectrumPersistenceService.count() == 0)
        }

        authenticate().post("""/webhooks/pull?query=metaData=q='name=="column" and value=="Acclaim RSLC C18 2.2um, 2.1x100mm, Thermo"' and metaData=q='name=="collision energy" and value=="Ramp 21.9-32.9 eV"'""").then().statusCode(200)

        eventually(timeout(50 seconds), interval(1000 millis)) {
          logger.info(s"local db is now: ${spectrumPersistenceService.count()} spectra")
          assert(spectrumPersistenceService.count() >= 3)
        }

      }


      "be able to download a specific query from the remote, with a not so small amount of results" in {
        spectrumPersistenceService.deleteAll()
        eventually(timeout(50 seconds), interval(1000 millis)) {
          logger.info(s"local db is now: ${spectrumPersistenceService.count()} spectra")
          assert(spectrumPersistenceService.count() == 0)
        }

        authenticate().post("""/webhooks/pull?query=metaData=q='name=="collision energy" and value=="50 eV"' and metaData=q='name=="ionization mode" and value=="positive"' and metaData=q='name=="column temperature" and value=="RT"'""").then().statusCode(200)

        eventually(timeout(50 seconds), interval(1000 millis)) {
          logger.info(s"local db is now: ${spectrumPersistenceService.count()} spectra")
          assert(spectrumPersistenceService.count() >= 84)
        }

      }


      "be able to download a specific query from the remote, with a medium amount of results" in {
        spectrumPersistenceService.deleteAll()
        eventually(timeout(50 seconds), interval(1000 millis)) {
          logger.info(s"local db is now: ${spectrumPersistenceService.count()} spectra")
          assert(spectrumPersistenceService.count() == 0)
        }

        authenticate().post("""/webhooks/pull?query=metaData=q='name=="collision energy" and value=="50 eV"'""").then().statusCode(200)

        eventually(timeout(50 seconds), interval(1000 millis)) {
          logger.info(s"local db is now: ${spectrumPersistenceService.count()} spectra")
          assert(spectrumPersistenceService.count() >= 496)
        }

      }

      "be able to download a specific query from the remote, with a large amount of results" in {
        spectrumPersistenceService.deleteAll()
        eventually(timeout(50 seconds)) {
          assert(spectrumPersistenceService.count() == 0)
        }

        authenticate().post("""/webhooks/pull?query=metaData=q='name=="instrument type" and value=="Quattro_QQQ"'""").then().statusCode(200)

        eventually(timeout(50 seconds), interval(1000 millis)) {
          logger.info(s"local db is now: ${spectrumPersistenceService.count()} spectra")
          assert(spectrumPersistenceService.count() >= 2269)
        }

      }
    }

    "be able to push all our data to all our slaves" must  {

      "require authentication" in {
        given().body(getValue).when().post(s"/webhooks/push").then().statusCode(401)
      }


      "require admin authentication" in {
        authenticate("test", "test-secret").body(getValue).when().post(s"/webhooks/push").then().statusCode(403)
      }

      "able to execute, without a timeout" in {

        //TODO update it once mona works correctly
        val queryCount = 100 //



        //reset the database
        spectrumPersistenceService.deleteAll()
        eventually(timeout(5 seconds)) {
          assert(spectrumPersistenceService.count() == 0)
        }

        webHookRepository.deleteAll()

        eventually(timeout(5 seconds)) {
          assert(webHookRepository.count() == 0)
        }

        webHookResultRepository.deleteAll()

        eventually(timeout(5 seconds)) {
          assert(webHookResultRepository.count() == 0)
        }


        //get some data from our main mona service
        authenticate().post("""/webhooks/pull?query=metaData=q='name=="collision energy" and value=="50 eV"'""").then().statusCode(200)

        eventually(timeout(50 seconds), interval(1000 millis)) {
          logger.info(s"local db is now: ${spectrumPersistenceService.count()} spectra")
          assert(spectrumPersistenceService.count() >= queryCount)
        }


        //add a slave

        authenticate().contentType("application/json; charset=UTF-8").body(getValue).when().post(s"/webhooks").then().statusCode(200)

        //push everything to our slave
        authenticate().body(getValue).when().post(s"/webhooks/push").then().statusCode(200)


        eventually(timeout(50 seconds), interval(1000 millis)) {
          logger.info(s"webhook events: ${webHookResultRepository.count()} spectra pushed")
          assert(webHookResultRepository.count() >= queryCount)
        }

      }

    }
    "be able to synchronize itself against a main mona server" should {

      "able to handle add events " in {

        spectrumPersistenceService.deleteAll()
        eventually(timeout(5 seconds)) {
          assert(spectrumPersistenceService.count() == 0)
        }
        //get count of spectra in db

        //add mona spectrum

        given().body(getValue).when().get(s"/webhooks/sync?id=AU100601&type=add").then().statusCode(200)


        //ensure the new spectra is now added
        eventually(timeout(5 seconds), interval(500 millis)) {
          spectrumPersistenceService.count() shouldBe 1
        }

      }

      "able to have it's webhook called, and start the synchronization" in {

        webHookRepository.deleteAll()

        eventually(timeout(5 seconds)) {
          assert(webHookRepository.count() == 0)
        }


        spectrumPersistenceService.deleteAll()

        eventually(timeout(5 seconds)) {
          assert(spectrumPersistenceService.count() == 0)
        }


        authenticate().contentType("application/json; charset=UTF-8").body(WebHook("mona-sync", s"http://localhost:${port}/rest/webhooks/sync", "mona synchronization hook")).when().post(s"/webhooks").then().statusCode(200)

        eventually(timeout(5 seconds)) {
          assert(webHookRepository.count() == 1)
        }

        val result: Array[WebHookResult] = authenticate().log().all(true).contentType("application/json; charset=UTF-8").when().post(s"/webhooks/trigger/AU100601/add").then().log().all(true).statusCode(200).extract().body().as(classOf[Array[WebHookResult]])

        result.foreach{ hook =>
          logger.info(s"${hook}")
        }
        assert(result.size == 1)
        eventually(timeout(20 seconds)) {
          assert(spectrumPersistenceService.count() == 1)
        }
      }


      "able to handle update events " in {

        spectrumPersistenceService.deleteAll()
        webHookRepository.deleteAll()

        eventually(timeout(5 seconds)) {
          assert(spectrumPersistenceService.count() == 0)
        }
        eventually(timeout(5 seconds)) {
          webHookRepository
        }

        //get count of spectra in db

        //add mona spectrum

        given().body(getValue).when().get(s"/webhooks/sync?id=AU100601&type=add").then().statusCode(200)

        //wait a bit

        //ensure the new spectra is now added
        eventually(timeout(5 seconds)) {
          spectrumPersistenceService.count() shouldBe 1
        }

        //add mona spectrum

        given().body(getValue).when().get(s"/webhooks/sync?id=AU100601&type=update").then().statusCode(200)


        //ensure the new spectra is now update
        eventually(timeout(5 seconds)) {
          Thread.sleep(2000)
          spectrumPersistenceService.count() shouldBe 1
        }

      }

      "throw an exception for wrongly specified events" in {

        given().body(getValue).when().get(s"/webhooks/sync?id=AU100601&type=thisIsNoEvent").then().statusCode(400)
      }
    }
  }
  override val requiresAuthForAllRequests: Boolean = false
}



