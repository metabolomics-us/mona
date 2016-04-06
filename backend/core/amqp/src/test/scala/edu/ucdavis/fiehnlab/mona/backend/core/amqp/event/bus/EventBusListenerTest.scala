package edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus

import java.util.Date
import java.util.concurrent.CountDownLatch

import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus.events.Event
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
@SpringApplicationConfiguration(classes = Array(classOf[TestConfig],classOf[BusConfig]))
class EventBusListenerTest extends WordSpec with Eventually {

  @Autowired
  val eventBus: EventBus[String] = null

  @Autowired
  val eventListener: List[EventBusTestListener] = null

  new TestContextManager(this.getClass()).prepareTestInstance(this)

  "EventBusListenerTest" should {

    "assure we have several evnet listeners" in {
      assert(eventListener.size == 10)
    }
    "received event" in {

      //each listener should have initially 2 events
      eventListener.foreach{ x=> x.events.getCount == 2}

      //send 2 events to the bus
      eventBus.sendEvent(new Event[String]("test-add", new Date,"custom"))
      eventBus.sendEvent(new Event[String]("test-add", new Date,"custom"))

      //after all events are processed we should have processed these 2 events by all listeners
      eventually(timeout(3 seconds)) {
        eventListener.foreach{ x=> x.events.getCount == 0}
      }
    }

  }
}

@Configuration
@EnableAutoConfiguration
class TestConfig {

  @Bean
  def eventBus: EventBus[String] = new EventBus[String]

  /**
    * define 10 different event listeners
    * @return
    */
  @Bean
  def listener: List[EventBusTestListener] = List[EventBusTestListener](
    new EventBusTestListener,
    new EventBusTestListener,
    new EventBusTestListener,
    new EventBusTestListener,
    new EventBusTestListener,
    new EventBusTestListener,
    new EventBusTestListener,
    new EventBusTestListener,
    new EventBusTestListener,
    new EventBusTestListener
  )

}

class EventBusTestListener extends EventBusListener[String] {
  val events = new CountDownLatch(2)

  /**
    * an element has been received from the bus and should be now processed
    *
    * @param event
    */
  override def received(event: Event[String]): Unit = events.countDown()
}
