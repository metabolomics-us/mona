package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.listener

import java.io.InputStreamReader
import java.util.Date

import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus.{EventBus, ReceivedEventCounter}
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.config.Notification
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.event.Event
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.AbstractSpringControllerTest
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.controller.TestConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.repository.WebHookRepository
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.types.WebHook
import org.junit.runner.RunWith
import org.scalatest.concurrent.Eventually
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import scala.concurrent.duration._

/**
  * Created by wohlgemuth on 4/8/16.
  */

@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[TestConfig]))
class WebHookEventBusListenerTest extends AbstractSpringControllerTest with Eventually{


  @Autowired
  val webHookRepository: WebHookRepository = null

  @Autowired
  val notificationCounter: ReceivedEventCounter[Notification] = null

  @Autowired
  val eventBus: EventBus[Spectrum] = null

  //required for spring and scala tes
  new TestContextManager(this.getClass()).prepareTestInstance(this)

  val input = new InputStreamReader(getClass.getResourceAsStream("/monaRecord.json"))

  val reader = JSONDomainReader.create[Spectrum]

  val spectrum: Spectrum = reader.read(input)

  "the webhook event listener" must {

    "create a webhook" in {
      webHookRepository.deleteAll()
      webHookRepository.save(WebHook("test", s"http://localhost:${port}/info?id=", "none provided"))
    }

    "event bus needs to be of type Spectrum" in {
      assert(eventBus.isInstanceOf[EventBus[Spectrum]])
    }
    "and trigger it on sending an update event" in {

      val notificationCount = notificationCounter.getEventCount
      eventBus.sendEvent(Event[Spectrum](spectrum,new Date(),Event.UPDATE))

      //we should get an information that the notification counter received an event
      eventually(timeout(10 seconds)) {
        assert(notificationCounter.getEventCount == notificationCount +  1)
      }

    }

    "and trigger it on sending an delete event" in {

      val notificationCount = notificationCounter.getEventCount
      eventBus.sendEvent(Event[Spectrum](spectrum,new Date(),Event.DELETE))

      //we should get an information that the notification counter received an event
      eventually(timeout(10 seconds)) {
        assert(notificationCounter.getEventCount == notificationCount +  1)
      }

    }

    "and trigger it on sending an insert event" in {

      val notificationCount = notificationCounter.getEventCount
      eventBus.sendEvent(Event[Spectrum](spectrum,new Date(),Event.ADD))

      //we should get an information that the notification counter received an event
      eventually(timeout(10 seconds)) {
        assert(notificationCounter.getEventCount == notificationCount +  1)
      }

    }

  }


}
