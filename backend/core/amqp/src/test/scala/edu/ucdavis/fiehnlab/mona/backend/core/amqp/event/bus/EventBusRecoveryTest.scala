package edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus

import java.io.InputStreamReader
import java.util.Date
import java.util.concurrent.CountDownLatch
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.event.Event
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import org.scalatest.concurrent.Eventually
import org.scalatest.wordspec.AnyWordSpec
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitAdmin
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.{Bean, Configuration, Import}
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.config.BusConfig
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Pins the behaviour the bus declarations exist for. A broker restart deletes the exchange and, once the
  * consumer drops with it, the auto-delete listener queue as well, while the application itself keeps
  * running and so never re-runs the @PostConstruct that originally declared them. Nothing then rebuilt the
  * topology and every published event was silently dropped until the services were restarted by hand
  */
@SpringBootTest(classes = Array(classOf[RecoveryTestConfig]))
@ActiveProfiles(Array("test"))
class EventBusRecoveryTest extends AnyWordSpec with Eventually {

  @Autowired
  val eventBus: EventBus[Spectrum] = null

  @Autowired
  val listener: RecoveryTestListener = null

  @Autowired
  val rabbitAdmin: RabbitAdmin = null

  @Autowired
  val connectionFactory: CachingConnectionFactory = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "an event bus whose topology was destroyed underneath a running application" should {

    "rebuild it on the next connection and keep delivering events" in {
      val reader: JSONDomainReader[Spectrum] = JSONDomainReader.create[Spectrum]
      val spectrum: Spectrum = reader.read(new InputStreamReader(getClass.getResourceAsStream("/monaRecord.json")))

      assert(listener.events.getCount == 1)

      // What a broker restart does to the bus, without restarting the broker. Dropping the connection also
      // takes the auto-delete queue with it, since its only consumer goes away
      rabbitAdmin.deleteExchange(eventBus.busName)
      connectionFactory.resetConnection()

      // Events published before the container has reconnected are genuinely lost, so keep publishing until
      // one lands. Without the declarations being re-registered no event ever arrives
      eventually(timeout(30 seconds), interval(1 second)) {
        eventBus.sendEvent(Event[Spectrum](spectrum, new Date, "custom"))
        assert(listener.events.getCount == 0)
      }
    }
  }
}

@Configuration
@EnableAutoConfiguration
@Import(Array(classOf[BusConfig]))
class RecoveryTestConfig {

  @Bean
  def eventBus: EventBus[Spectrum] = new EventBus[Spectrum]("mona-recovery-test-bus")

  @Bean
  def recoveryListener(eventBus: EventBus[Spectrum]): RecoveryTestListener = new RecoveryTestListener(eventBus)
}

class RecoveryTestListener(override val eventBus: EventBus[Spectrum]) extends EventBusListener[Spectrum](eventBus) {

  val events = new CountDownLatch(1)

  override def received(event: Event[Spectrum]): Unit = events.countDown()
}
