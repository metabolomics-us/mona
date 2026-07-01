package edu.ucdavis.fiehnlab.mona.backend.core.curation.runner

import org.scalatest.flatspec.AnyFlatSpec
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer

/**
  * Verifies that the configurable curation concurrency (mona.curation.concurrency) is applied to the main
  * curation listener container. The container exposes no public getter for the consumer counts, so they are
  * read reflectively
  */
class CurationConcurrencyConfigSpec extends AnyFlatSpec {

  private def intField(container: SimpleMessageListenerContainer, name: String): Int = {
    val field = classOf[SimpleMessageListenerContainer].getDeclaredField(name)
    field.setAccessible(true)
    field.get(container).asInstanceOf[Number].intValue()
  }

  "CurationRunner.container" should "apply the configured concurrency to the listener container" in {
    val runner = new CurationRunner {
      override val queueName: String = "curation-queue"
      override val curationConcurrency: Int = 8
    }

    val container: SimpleMessageListenerContainer =
      runner.container(new CachingConnectionFactory(), new CurationListener(null, null, null, null, null))

    assert(intField(container, "concurrentConsumers") == 8)
    assert(intField(container, "maxConcurrentConsumers") == 8)
  }
}
