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
import org.springframework.boot.context.embedded.LocalServerPort
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Created by wohlgemuth on 4/8/16.
  */

@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[TestConfig]), webEnvironment = WebEnvironment.DEFINED_PORT)
class WebHookEventBusListenerTest extends AbstractSpringControllerTest with Eventually {

  @LocalServerPort
  private val port = 0

  @Autowired
  val webHookRepository: WebHookRepository = null

  @Autowired
  val notificationCounter: ReceivedEventCounter[Notification] = null

  @Autowired
  val eventBus: EventBus[Spectrum] = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "the webhook event listener" must {
    val input = new InputStreamReader(getClass.getResourceAsStream("/monaRecord.json"))
    val reader: JSONDomainReader[Spectrum] = JSONDomainReader.create[Spectrum]
    val spectrum: Spectrum = reader.read(input)

    "create a webhook" in {
      webHookRepository.deleteAll()
      webHookRepository.save(WebHook("test", s"http://localhost:$port/info?id=", "none provided", "test"))
    }

    "event bus needs to be of type Spectrum" in {
      assert(eventBus.isInstanceOf[EventBus[Spectrum]])
    }

    "and trigger it on sending an update event" in {
      val notificationCount = notificationCounter.getEventCount
      eventBus.sendEvent(Event[Spectrum](spectrum, new Date(), Event.UPDATE))

      //we should get an information that the notification counter received an event
      eventually(timeout(10 seconds)) {
        assert(notificationCounter.getEventCount == notificationCount + 1)
      }
    }

    "and trigger it on sending an delete event" in {
      val notificationCount = notificationCounter.getEventCount
      eventBus.sendEvent(Event[Spectrum](spectrum, new Date(), Event.DELETE))

      //we should get an information that the notification counter received an event
      eventually(timeout(100 seconds)) {
        assert(notificationCounter.getEventCount == notificationCount + 1)
      }
    }

    "and trigger it on sending an insert  event" in {
      val notificationCount = notificationCounter.getEventCount
      eventBus.sendEvent(Event[Spectrum](spectrum, new Date(), Event.ADD))

      //we should get an information that the notification counter received an event
      eventually(timeout(10 seconds)) {
        assert(notificationCounter.getEventCount == notificationCount + 1)
      }
    }
  }
}
