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
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.{SpringApplicationConfiguration, WebIntegrationTest}
import org.springframework.context.annotation.{Bean, Configuration, Import}
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[TestConfig]))
class WebhookControllerTest extends AbstractGenericRESTControllerTest[WebHook]("/webhooks") {

  @Autowired
  val webHookRepository:WebHookRepository = null

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

    "be able to trigger the registered external urls" in {
      webHookRepository.deleteAll()
      authenticate().contentType("application/json; charset=UTF-8").body(getValue).when().post(s"/webhooks").then().statusCode(200)

      val result:Array[WebHookResult] = given().log().all(true).contentType("application/json; charset=UTF-8").when().get(s"/webhooks/trigger/${getId}").then().log().all(true).statusCode(200).extract().body().as(classOf[Array[WebHookResult]])

      assert(result.size == 1)

      result.foreach { x => assert(x.success) }

    }


    "be able to trigger the registered external urls and not crash in case they cant be reached" in {
      webHookRepository.deleteAll()
      authenticate().contentType("application/json; charset=UTF-8").body(WebHook("i can't be reached","http://localhost:21234/rest","none provided")).when().post(s"/webhooks").then().statusCode(200)

      val result:Array[WebHookResult] = given().log().all(true).contentType("application/json; charset=UTF-8").when().get(s"/webhooks/trigger/${getId}").then().log().all(true).statusCode(200).extract().body().as(classOf[Array[WebHookResult]])

      assert(result.size == 1)

      result.foreach { x =>
        assert(!x.success)
        assert(x.error != "")

      }
    }


    "be able to trigger the registered external urls and not crash in case they don't exist" in {
      webHookRepository.deleteAll()
      authenticate().contentType("application/json; charset=UTF-8").body(WebHook("i dont exist",s"http://localhost:${port}/info/id/","this tosses a 401")).when().post(s"/webhooks").then().statusCode(200)

      val result:Array[WebHookResult] = given().log().all(true).contentType("application/json; charset=UTF-8").when().get(s"/webhooks/trigger/${getId}").then().log().all(true).statusCode(200).extract().body().as(classOf[Array[WebHookResult]])

      assert(result.size == 1)

      result.foreach { x =>
        assert(!x.success)
        assert(x.error != "")

      }
    }


  }

}



