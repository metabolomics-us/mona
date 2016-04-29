package edu.ucdavis.fiehnlab.mona.backend.core.curation.runner

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.config.BusConfig
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.listener.GenericMessageListener
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.Workflow
import edu.ucdavis.fiehnlab.mona.backend.curation.config.CurationConfig
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.batch.item.ItemProcessor
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.{CommandLineRunner, SpringApplication}
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.context.annotation.{Bean, Import}
import org.springframework.security.config.annotation.web.configuration.{EnableWebSecurity, WebSecurityConfigurerAdapter}
import org.springframework.stereotype.Component

/**
  * This class starts the curation service and let's it listen in the background for messages
  * it also exposes a couple of rest points, which allow simple scheduling of messages
  */
@SpringBootApplication
@EnableDiscoveryClient
@EnableWebSecurity
@Import(Array(classOf[CurationConfig],classOf[BusConfig]))
class CurationRunner extends WebSecurityConfigurerAdapter with LazyLogging{

  @Autowired
  @Qualifier("spectra-curation-queue")
  val queueName:String = null

  @Bean
  def curationListener(curationWorkflow: ItemProcessor[Spectrum,Spectrum]): CurationListener = new CurationListener(curationWorkflow)

  @Bean
  def container(connectionFactory: ConnectionFactory, listener: CurationListener,messageConverter:MessageConverter): SimpleMessageListenerContainer = {
    logger.info(s"connecting to queue: ${queueName}")
    val container = new SimpleMessageListenerContainer()
    container.setConnectionFactory(connectionFactory)
    container.setMessageListener(listener)
    container.setMessageConverter(messageConverter)
    container.setQueueNames(queueName)

    container
  }

}

/**
  * our local server, which should be connecting to eureka, etc
  */
object CurationRunner extends App {
  new SpringApplication(classOf[CurationRunner]).run()
}

/**
  * listens to our queue and does our processing
  */
class CurationListener(workflow: ItemProcessor[Spectrum,Spectrum]) extends GenericMessageListener[Spectrum] with LazyLogging {

  override def handleMessage(spectra: Spectrum) = {
    logger.info(s"received spectra: ${spectra.id}")
    workflow.process(spectra)
    logger.info(s"curated spectra: ${spectra.id}")
  }
}