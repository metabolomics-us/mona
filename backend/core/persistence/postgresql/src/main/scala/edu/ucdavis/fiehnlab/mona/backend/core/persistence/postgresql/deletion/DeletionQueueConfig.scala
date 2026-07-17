package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.deletion

import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.{Bean, Configuration, Import, Profile}

/**
  * Declares the durable spectra deletion queue's name and Queue bean, so any service that needs to
  * publish deletion requests (persistence-server's admin delete, download-scheduler's upload delete)
  * can resolve them via @Qualifier("spectra-deletion-queue") without also starting a consumer.
  * Services that only publish should import this directly; DeletionQueueConfig below imports it too
  * and adds the consumer, and must stay limited to persistence-server
  */
@Configuration
@Profile(Array("mona.persistence"))
class DeletionQueueDeclarationConfig {

  @Bean(name = Array("spectra-deletion-queue"))
  def deletionQueueName: String = "spectra-deletion-queue"

  @Bean(name = Array("spectra-deletion-queue-instance"))
  def deletionQueue: Queue = new Queue(deletionQueueName, true)
}

/**
  * Wires the SpectrumDeletionListener consumer to the deletion queue declared above.
  * Concurrency is left at the default of one consumer so deletions run sequentially and never
  * contend with each other on the database. Must run in exactly one place (persistence-server,
  * where the delete controller lives) -- importing this in more than one service starts competing
  * consumers racing for the same messages
  */
@Configuration
@Import(Array(classOf[DeletionQueueDeclarationConfig]))
@Profile(Array("mona.persistence"))
class DeletionQueueConfig {

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
