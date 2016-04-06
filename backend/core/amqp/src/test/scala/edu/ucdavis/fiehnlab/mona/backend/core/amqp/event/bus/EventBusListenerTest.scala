package edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus

import java.util.concurrent.CountDownLatch

import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus.events.{AddEvent, DeleteEvent, Event, UpdateEvent}
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.config.BusConfig
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.concurrent.Eventually
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import scala.concurrent.duration._

/**
  * Created by wohlg on 4/6/2016.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[TestConfig], classOf[BusConfig]))
class EventBusListenerTest extends WordSpec with Eventually {

  @Autowired
  val eventBus: EventBus[String] = null

  @Autowired
  val eventListener: EventBusTestListener = null

  new TestContextManager(this.getClass()).prepareTestInstance(this)

  "EventBusListenerTest" should {

    "received add event" in {

      eventBus.sendEvent(new AddEvent[String]("test-add"))
      eventBus.sendEvent(new AddEvent[String]("test-add"))
      eventBus.sendEvent(new UpdateEvent[String]("test-up"))
      eventBus.sendEvent(new UpdateEvent[String]("test-up"))
      eventBus.sendEvent(new DeleteEvent[String]("test-del"))
      eventBus.sendEvent(new DeleteEvent[String]("test-del"))


      eventually(timeout(10 seconds)) {
        assert(eventListener.addedEvents.getCount == 0)
      }
    }
    "received update event" in {

      eventBus.sendEvent(new UpdateEvent[String]("test-up"))
      eventBus.sendEvent(new UpdateEvent[String]("test-up"))


      eventually(timeout(10 seconds)) {
        assert(eventListener.updatedEvents.getCount == 0)
      }
    }
    "received del event" in {


      eventBus.sendEvent(new DeleteEvent[String]("test-del"))
      eventBus.sendEvent(new DeleteEvent[String]("test-del"))


      eventually(timeout(10 seconds)) {
        assert(eventListener.deletedEvents.getCount == 0)
      }
    }

  }
}

@Configuration
@EnableAutoConfiguration
class TestConfig {

  @Bean
  def eventBus: EventBus[String] = new EventBus[String]

  @Bean
  def listener: EventBusTestListener = new EventBusTestListener


}

class EventBusTestListener extends EventBusListener[String] {
  val addedEvents = new CountDownLatch(2)
  val updatedEvents = new CountDownLatch(2)
  val deletedEvents = new CountDownLatch(2)

  /**
    * an entry was added to the system
    *
    * @param event
    */
  override def added(event: Event[String]): Unit = {
    addedEvents.countDown()
    assert(event.content == "test-add")
  }

  /**
    * the event was updated in the system
    *
    * @param event
    */
  override def updated(event: Event[String]): Unit = {
    updatedEvents.countDown()
    assert(event.content == "test-up")
  }

  /**
    * an entry was deleted from the system
    *
    * @param event
    */
  override def deleted(event: Event[String]): Unit = {
    deletedEvents.countDown()
    assert(event.content == "test-del")

  }
}