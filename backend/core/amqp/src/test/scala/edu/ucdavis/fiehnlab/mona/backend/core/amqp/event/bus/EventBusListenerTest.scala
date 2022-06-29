package edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus

import java.io.InputStreamReader
import java.util.Date
import java.util.concurrent.CountDownLatch
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.config.{MonaEventBusCounterConfiguration, MonaNotificationBusCounterConfiguration}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.SpectrumResult
import edu.ucdavis.fiehnlab.mona.backend.core.domain.event.Event
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.concurrent.Eventually
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.test.context.{ActiveProfiles, TestContextManager}
import org.springframework.test.context.junit4.SpringRunner

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.reflect.ClassTag

/**
  * Created by wohlg on 4/6/2016.
  */
@SpringBootTest(classes = Array(classOf[StringTestConfig], classOf[MonaNotificationBusCounterConfiguration], classOf[MonaEventBusCounterConfiguration]))
@ActiveProfiles(Array("test"))
class EventBusListenerTest extends AnyWordSpec with Eventually {

  @Autowired
  val eventBus: EventBus[SpectrumResult] = null

  @Autowired
  val eventListener: List[EventBusTestListener[SpectrumResult]] = null

  @Autowired
  val eventCounter: ReceivedEventCounter[SpectrumResult] = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "EventBusListenerTest" should {

    val reader: JSONDomainReader[SpectrumResult] = JSONDomainReader.create[SpectrumResult]
    val input = new InputStreamReader(getClass.getResourceAsStream("/monaRecord.json"))
    val spectrum: SpectrumResult = reader.read(input)

    "assure we have several event listeners" in {
      assert(eventListener.size == 5)
    }

    "received event" in {
      //each listener should have initially N events
      eventListener.foreach { x => assert(x.events.getCount == 2) }

      //send 2 events to the bus
      eventBus.sendEvent(new Event[SpectrumResult](spectrum, new Date, "custom"))
      eventBus.sendEvent(new Event[SpectrumResult](spectrum, new Date, "custom"))

      //after all events are processed we should have processed these 2 events by all listeners
      eventually(timeout(3 seconds)) {
        eventListener.foreach { x => assert(x.events.getCount == 0) }
      }
    }

    "ensure that the total send events match the internal counter" in {
      eventually(timeout(3 seconds)) {
        assert(eventCounter.getEventCount == 2)
      }
    }
  }
}

@Configuration
@EnableAutoConfiguration
class StringTestConfig {

  /**
    * our bus for testing
    *
    * @return
    */
  @Bean
  def eventBus: EventBus[SpectrumResult] = new EventBus[SpectrumResult]("my-test-bus")

  @Bean
  def listenerA(eventBus: EventBus[SpectrumResult]): EventBusTestListener[SpectrumResult] = new EventBusTestListener[SpectrumResult](eventBus)

  @Bean
  def listenerB(eventBus: EventBus[SpectrumResult]): EventBusTestListener[SpectrumResult] = new EventBusTestListener[SpectrumResult](eventBus)

  @Bean
  def listenerC(eventBus: EventBus[SpectrumResult]): EventBusTestListener[SpectrumResult] = new EventBusTestListener[SpectrumResult](eventBus)

  @Bean
  def listenerD(eventBus: EventBus[SpectrumResult]): EventBusTestListener[SpectrumResult] = new EventBusTestListener[SpectrumResult](eventBus)

  @Bean
  def listenerE(eventBus: EventBus[SpectrumResult]): EventBusTestListener[SpectrumResult] = new EventBusTestListener[SpectrumResult](eventBus)

  /**
    * just a collection of all our bus clients to simulate some behaviour
    *
    * @param eventBus
    * @return
    */
  @Bean
  def listener(eventBus: EventBus[SpectrumResult]): List[EventBusTestListener[SpectrumResult]] = listenerA(eventBus) :: listenerB(eventBus) :: listenerC(eventBus) :: listenerD(eventBus) :: listenerE(eventBus) :: List()

  /**
    * an event listener to just monitor send events over the bus
    *
    * @return
    */
  @Bean
  def eventCounter(eventBus: EventBus[SpectrumResult]): ReceivedEventCounter[SpectrumResult] = new ReceivedEventCounter[SpectrumResult](eventBus)
}

class EventBusTestListener[T: ClassTag](override val eventBus: EventBus[T]) extends EventBusListener[T](eventBus) {
  val events = new CountDownLatch(2)

  /**
    * an element has been received from the bus and should be now processed
    *
    * @param event
    */
  override def received(event: Event[T]): Unit = events.countDown()
}
