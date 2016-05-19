package edu.ucdavis.fiehnlab.mona.backend.core.curation.listener

import java.io.InputStreamReader
import java.util.Date

import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus.EventBus
import edu.ucdavis.fiehnlab.mona.backend.core.curation.CurationScheduler
import edu.ucdavis.fiehnlab.mona.backend.core.curation.service.TestCurrationRunner
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.event.Event
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.AbstractSpringControllerTest
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.concurrent.Eventually
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import scala.concurrent.duration._

/**
  * Created by wohlg on 4/12/2016.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[CurationScheduler]))
class CurrationEventBusListenerTest extends AbstractSpringControllerTest with Eventually {

  @Autowired
  val eventBus: EventBus[Spectrum] = null

  @Autowired
  val testCurrationRunner: TestCurrationRunner = null

  new TestContextManager(this.getClass()).prepareTestInstance(this)

  "CurrationEventBusListenerTest" should {


    val reader = JSONDomainReader.create[Spectrum]

    val input = new InputStreamReader(getClass.getResourceAsStream("/monaRecord.json"))

    val spectrum: Spectrum = reader.read(input)


    "received an update event" in {
      testCurrationRunner.messageReceived = false

      eventBus.sendEvent(Event(spectrum, new Date(), Event.UPDATE))

      eventually(timeout(100 seconds)) {
      //it should never receive an event, since we don't listen for updates
        assert(!testCurrationRunner.messageReceived)
      }

    }


    "received an delete event" in {
      eventBus.sendEvent(Event(spectrum, new Date(), Event.DELETE))
      testCurrationRunner.messageReceived = false

      eventually(timeout(100 seconds)) {
        //it should never receive an event, since we don't listen for deletes
        assert(!testCurrationRunner.messageReceived)
      }

    }

    "received an add event" in {
      testCurrationRunner.messageReceived = false
      eventBus.sendEvent(Event(spectrum, new Date(), Event.ADD))

      eventually(timeout(100 seconds)) {
        assert(testCurrationRunner.messageReceived)
      }


    }

  }
}

