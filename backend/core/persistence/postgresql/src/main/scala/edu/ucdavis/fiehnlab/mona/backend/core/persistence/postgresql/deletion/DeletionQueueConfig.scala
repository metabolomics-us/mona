package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.deletion

import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.context.annotation.{Bean, Configuration, Profile}

/**
  * Declares the durable spectra deletion queue and wires the SpectrumDeletionListener to it.
  * The queue is durable so requests survive a broker restart, and the matching DeletionJob row
  * keeps the work recoverable. Concurrency is left at the default of one consumer so deletions
  * run sequentially and never contend with each other on the database
  */
@Configuration
@Profile(Array("mona.persistence"))
class DeletionQueueConfig {

  @Bean(name = Array("spectra-deletion-queue"))
  def deletionQueueName: String = "spectra-deletion-queue"

  @Bean(name = Array("spectra-deletion-queue-instance"))
  def deletionQueue: Queue = new Queue(deletionQueueName, true)

  // Registered here as a bean (rather than a scanned @Component) so a single @Import of this config
  // wires the listener together with its queue and container. Spring still injects its @Autowired fields
  @Bean
  def spectrumDeletionListener: SpectrumDeletionListener = new SpectrumDeletionListener

  @Bean
  @Qualifier("message-listener-spectra-deletion")
  def spectraDeletionListenerContainer(connectionFactory: ConnectionFactory,
                                       @Qualifier("spectra-deletion-queue-instance") deletionQueue: Queue,
                                       spectrumDeletionListener: SpectrumDeletionListener): SimpleMessageListenerContainer = {
    val container = new SimpleMessageListenerContainer()
    container.setConnectionFactory(connectionFactory)
    container.setMessageListener(spectrumDeletionListener)
    container.setQueues(deletionQueue)
    container
  }
}
