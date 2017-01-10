package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.controller

import com.jayway.restassured.RestAssured._
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.config.{BusConfig, MonaEventBusConfiguration, MonaNotificationBusConfiguration}
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.service.MongoLoginService
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.types.TokenSecret
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.AbstractGenericRESTControllerTest
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.config.WebHookSecurity
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.repository.WebHookRepository
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
class WebhookControllerTest extends AbstractGenericRESTControllerTest[WebHook]("/webhooks") with Eventually with ShouldMatchers{

  @Autowired
  val webHookRepository:WebHookRepository = null

  @Autowired
  val spectrumPersistenceService: SpectrumPersistenceService = null

  //required for spring and scala tes
  new TestContextManager(this.getClass()).prepareTestInstance(this)

  /**
    * object to use for gets
    *
    * @return
    */
  override def getValue: WebHook = WebHook("my test hook", s"http://localhost:${port}/info?id=", "just for testing")

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

      val result:Array[WebHookResult] = given().log().all(true).contentType("application/json; charset=UTF-8").when().get(s"/webhooks/trigger/${getId}/add").then().log().all(true).statusCode(200).extract().body().as(classOf[Array[WebHookResult]])

      assert(result.size == 1)

      result.foreach { x => assert(x.success) }

    }


    "be able to trigger the registered external urls and not crash in case they cant be reached" in {
      webHookRepository.deleteAll()
      authenticate().contentType("application/json; charset=UTF-8").body(WebHook("i can't be reached","http://localhost:21234/rest","none provided")).when().post(s"/webhooks").then().statusCode(200)

      val result:Array[WebHookResult] = given().log().all(true).contentType("application/json; charset=UTF-8").when().get(s"/webhooks/trigger/${getId}/add").then().log().all(true).statusCode(200).extract().body().as(classOf[Array[WebHookResult]])

      assert(result.size == 1)

      result.foreach { x =>
        assert(!x.success)
        assert(x.error != "")

      }
    }


    "be able to trigger the registered external urls and not crash in case they don't exist" in {
      webHookRepository.deleteAll()
      authenticate().contentType("application/json; charset=UTF-8").body(WebHook("i dont exist",s"http://localhost:${port}/info/id/","this tosses a 401")).when().post(s"/webhooks").then().statusCode(200)

      val result:Array[WebHookResult] = given().log().all(true).contentType("application/json; charset=UTF-8").when().get(s"/webhooks/trigger/${getId}/add").then().log().all(true).statusCode(200).extract().body().as(classOf[Array[WebHookResult]])

      assert(result.size == 1)

      result.foreach { x =>
        assert(!x.success)
        assert(x.error != "")

      }
    }
    "be able to pull a copy from the remote server" should {

      "require authentication" in {
        given().body(getValue).when().post(s"/webhooks/pull").then().statusCode(401)
      }

      "be able to download a specific query from the remote, with a small amount of results" in {
        spectrumPersistenceService.deleteAll()
        eventually(timeout(50 seconds), interval(1000 millis)){
          logger.info(s"local db is now: ${spectrumPersistenceService.count()} spectra")
          assert(spectrumPersistenceService.count() == 0)
        }

        authenticate().post("""/webhooks/pull?query=metaData=q='name=="column" and value=="Acclaim RSLC C18 2.2um, 2.1x100mm, Thermo"' and metaData=q='name=="collision energy" and value=="Ramp 21.9-32.9 eV"'""").then().statusCode(200)

        eventually(timeout(50 seconds), interval(1000 millis)){
          logger.info(s"local db is now: ${spectrumPersistenceService.count()} spectra")
          assert(spectrumPersistenceService.count() >= 3)
        }

      }


      "be able to download a specific query from the remote, with a not so small amount of results" in {
        spectrumPersistenceService.deleteAll()
        eventually(timeout(50 seconds), interval(1000 millis)){
          logger.info(s"local db is now: ${spectrumPersistenceService.count()} spectra")
          assert(spectrumPersistenceService.count() == 0)
        }

        authenticate().post("""/webhooks/pull?query=metaData=q='name=="collision energy" and value=="50 eV"' and metaData=q='name=="ionization mode" and value=="positive"' and metaData=q='name=="column temperature" and value=="RT"'""").then().statusCode(200)

        eventually(timeout(50 seconds), interval(1000 millis)){
          logger.info(s"local db is now: ${spectrumPersistenceService.count()} spectra")
          assert(spectrumPersistenceService.count() >= 84)
        }

      }


      "be able to download a specific query from the remote, with a medium amount of results" in {
        spectrumPersistenceService.deleteAll()
        eventually(timeout(50 seconds), interval(1000 millis)){
          logger.info(s"local db is now: ${spectrumPersistenceService.count()} spectra")
          assert(spectrumPersistenceService.count() == 0)
        }

        authenticate().post("""/webhooks/pull?query=metaData=q='name=="collision energy" and value=="50 eV"'""").then().statusCode(200)

        eventually(timeout(50 seconds), interval(1000 millis)){
          logger.info(s"local db is now: ${spectrumPersistenceService.count()} spectra")
          assert(spectrumPersistenceService.count() >= 496)
        }

      }
      "be able to download a specific query from the remote, with a large amount of results" in {
        spectrumPersistenceService.deleteAll()
        eventually(timeout(50 seconds)) {
          assert(spectrumPersistenceService.count() == 0)
        }

        authenticate().post("""/webhooks/pull?query=metaData=q='name=="flow gradient" and value=="99/1 at 0-1 min, 61/39 at 3 min, 0.1/99.9 at 14-16 min, 99/1 at 16.1-20 min"'""").then().statusCode(200)

        eventually(timeout(50 seconds), interval(1000 millis)){
          logger.info(s"local db is now: ${spectrumPersistenceService.count()} spectra")
          assert(spectrumPersistenceService.count() >= 1492)
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
        given().body(getValue).when().get(s"/webhooks/sync/AU100601/add").then().statusCode(200)

        //ensure the new spectra is now added
        eventually(timeout(5 seconds), interval(500 millis)){
          spectrumPersistenceService.count() shouldBe 1
        }

      }


      "able to handle update events " in {

        spectrumPersistenceService.deleteAll()

        eventually(timeout(5 seconds)) {
          assert(spectrumPersistenceService.count() == 0)
        }

        //get count of spectra in db

        //add mona spectrum
        given().body(getValue).when().get(s"/webhooks/sync/AU100601/add").then().statusCode(200)

        //wait a bit

        //ensure the new spectra is now added
        eventually(timeout(5 seconds)){
          spectrumPersistenceService.count() shouldBe 1
        }

        //add mona spectrum
        given().body(getValue).when().get(s"/webhooks/sync/AU100601/update").then().statusCode(200)


        //ensure the new spectra is now update
        eventually(timeout(5 seconds)){
          Thread.sleep(2000)
          spectrumPersistenceService.count() shouldBe 1
        }

      }

      "throw an exception for wrongly specified events" in {
        given().body(getValue).when().get(s"/webhooks/sync/AU100601/thisIsNoEvent").then().statusCode(400)
      }
    }
  }
  override val requiresAuthForAllRequests: Boolean = false
}



