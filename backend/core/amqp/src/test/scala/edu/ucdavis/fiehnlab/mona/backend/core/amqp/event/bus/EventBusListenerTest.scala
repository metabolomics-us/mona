package edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus

import java.util.Date
import java.util.concurrent.CountDownLatch

import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.config.BusConfig
import edu.ucdavis.fiehnlab.mona.backend.core.domain.event.Event
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

  @Autowired
  val eventCounter:ReceivedEventCounter[String] = null

  new TestContextManager(this.getClass()).prepareTestInstance(this)

  "EventBusListenerTest" should {

    "assure we have several event listeners" in {
      assert(eventListener.size == 5)
    }
    "received event" in {

      //each listener should have initially 2 events
      eventListener.foreach{ x=> assert(x.events.getCount == 2)}

      //send 2 events to the bus
      eventBus.sendEvent(new Event[String]("test-add", new Date,"custom"))
      eventBus.sendEvent(new Event[String]("test-add", new Date,"custom"))

      //after all events are processed we should have processed these 2 events by all listeners
      eventually(timeout(3 seconds)) {
        eventListener.foreach{ x=> assert(x.events.getCount == 0)}
      }
    }

    "ensure that the total send events match the internal counter" in {
      assert(eventCounter.getEventCount == 2)
    }

  }
}

@Configuration
@EnableAutoConfiguration
class TestConfig {

  /**
    * our bus for testing
    * @return
    */
  @Bean
  def eventBus: EventBus[String] = new EventBus[String]("my-test-bus")

  @Bean
  def listenerA(eventBus: EventBus[String]) :EventBusTestListener =  new EventBusTestListener(eventBus)
  @Bean
  def listenerB(eventBus: EventBus[String]):EventBusTestListener =  new EventBusTestListener(eventBus)
  @Bean
  def listenerC(eventBus: EventBus[String]):EventBusTestListener =  new EventBusTestListener(eventBus)
  @Bean
  def listenerD(eventBus: EventBus[String]):EventBusTestListener =  new EventBusTestListener(eventBus)
  @Bean
  def listenerE(eventBus: EventBus[String]):EventBusTestListener =  new EventBusTestListener(eventBus)

  /**
    * just a collection of all our bus clients to simulate some behaviour
    * @param eventBus
    * @return
    */
  @Bean
  def listener(eventBus: EventBus[String]) :List[EventBusTestListener] = listenerA(eventBus) :: listenerB(eventBus) :: listenerC(eventBus) :: listenerD(eventBus) :: listenerE(eventBus) :: List()

  /**
    * an event listener to just monitor send events over the bus
 *
    * @return
    */
  @Bean
  def eventCounter(eventBus: EventBus[String]): ReceivedEventCounter[String] = new ReceivedEventCounter[String](eventBus)


}

class EventBusTestListener(override val eventBus: EventBus[String]) extends EventBusListener[String](eventBus) {
  val events = new CountDownLatch(2)

  /**
    * an element has been received from the bus and should be now processed
    *
    * @param event
    */
  override def received(event: Event[String]): Unit = events.countDown()
}
