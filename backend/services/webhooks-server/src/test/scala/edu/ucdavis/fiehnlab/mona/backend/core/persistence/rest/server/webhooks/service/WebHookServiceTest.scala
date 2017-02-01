package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.service

import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus.ReceivedEventCounter
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.config.Notification
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.AbstractSpringControllerTest
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.config.WebHookSecurity
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.controller.TestConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.repository.WebHookRepository
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.types.WebHook
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.concurrent.Eventually
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.{SpringApplicationConfiguration, WebIntegrationTest}
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import scala.concurrent.duration._

/**
  * Created by wohlgemuth on 4/8/16.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[TestConfig]))
class WebHookServiceTest extends AbstractSpringControllerTest with Eventually{


  @Autowired
  val webHookRepository:WebHookRepository = null

  @Autowired
  val notificationCounter:ReceivedEventCounter[Notification] = null

  @Autowired
  val webHookService:WebHookService = null

  //required for spring and scala tes
  new TestContextManager(this.getClass()).prepareTestInstance(this)

  "WebHookServiceTest" should {

    "trigger" in {

      webHookRepository.deleteAll()
      webHookRepository.save(WebHook("test",s"http://localhost:${port}/info?id="))

      val count = notificationCounter.getEventCount

      val result = webHookService.trigger("12345","test")

      assert(result.size == 1)

      eventually(timeout(100 seconds)) {
        assert(notificationCounter.getEventCount == count +  1)
      }

    }


  }
}