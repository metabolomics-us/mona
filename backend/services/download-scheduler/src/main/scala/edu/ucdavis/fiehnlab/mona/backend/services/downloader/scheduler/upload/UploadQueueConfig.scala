package edu.ucdavis.fiehnlab.mona.backend.services.downloader.scheduler.upload

import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.{Bean, Configuration, Profile}

/**
  * Declares the durable spectra upload queue and wires the UploadJobListener to it. The queue is
  * durable so requests survive a broker restart, and the matching UploadJob row keeps the work
  * recoverable. Concurrency is left at the default of one consumer so uploads are parsed and
  * persisted sequentially and never contend with each other on the database.
  *
  * Imported explicitly by DownloadScheduler (not scanned) so the upload consumer runs in exactly
  * one place, mirroring how RestServerConfig imports the deletion queue for the persistence server
  */
@Configuration
@Profile(Array("mona.persistence"))
class UploadQueueConfig {

  @Bean(name = Array("spectra-upload-queue"))
  def uploadQueueName: String = "spectra-upload-queue"

  @Bean(name = Array("spectra-upload-queue-instance"))
  def uploadQueue: Queue = new Queue(uploadQueueName, true)

  @Bean
  def uploadJobListener: UploadJobListener = new UploadJobListener

  @Bean
  @Qualifier("message-listener-spectra-upload")
  def uploadListenerContainer(connectionFactory: ConnectionFactory,
                              @Qualifier("spectra-upload-queue-instance") uploadQueue: Queue,
                              uploadJobListener: UploadJobListener): SimpleMessageListenerContainer = {
    val container = new SimpleMessageListenerContainer()
    container.setConnectionFactory(connectionFactory)
    container.setMessageListener(uploadJobListener)
    container.setQueues(uploadQueue)
    container
  }
}
