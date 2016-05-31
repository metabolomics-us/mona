package edu.ucdavis.fiehnlab.mona.backend.core.curation.runner

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.config.BusConfig
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.listener.GenericMessageListener
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.config.RestClientConfig
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.Workflow
import edu.ucdavis.fiehnlab.mona.backend.curation.config.CurationConfig
import edu.ucdavis.fiehnlab.mona.backend.curation.writer.RestRepositoryWriter
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.batch.item.ItemProcessor
import org.springframework.beans.factory.annotation.{Autowired, Qualifier, Value}
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.{CommandLineRunner, SpringApplication}
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.context.annotation.{Bean, Import}
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.configuration.{EnableWebSecurity, WebSecurityConfigurerAdapter}
import org.springframework.stereotype.Component

/**
  * This class starts the curation service and let's it listen in the background for messages
  * it also exposes a couple of rest points, which allow simple scheduling of messages
  */
@SpringBootApplication
@EnableWebSecurity
@EnableDiscoveryClient
@Import(Array(classOf[RestClientConfig],classOf[CurationConfig]))
@Order(1)
class CurationRunner extends WebSecurityConfigurerAdapter with LazyLogging {

  @Autowired
  @Qualifier("spectra-curation-queue")
  val queueName: String = null

  @Value("${mona.security.curation.token}")
  val token: String = null

  @Bean
  def restWriter : RestRepositoryWriter = {
    new RestRepositoryWriter(token)
  }

  @Bean
  def curationListener(curationWorkflow: ItemProcessor[Spectrum,Spectrum]): CurationListener = {
    new CurationListener(curationWorkflow,restWriter)
  }

  @Bean
  def container(connectionFactory: ConnectionFactory, listener: CurationListener,messageConverter:MessageConverter): SimpleMessageListenerContainer = {
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
class CurationListener(workflow: ItemProcessor[Spectrum,Spectrum],writer:RestRepositoryWriter) extends GenericMessageListener[Spectrum] with LazyLogging {

  override def handleMessage(spectra: Spectrum) = {
    try {
      logger.info(s"received spectra: ${spectra.id}")
      val result: Spectrum = workflow.process(spectra)
      logger.info(s"curated spectra: ${spectra.id}")
      writer.write(result)
      logger.info("saved spectra to system")
    }
    catch {
      case e:Exception => logger.info(s"exception during curation of spectra, fail silently: ${e.getMessage}",e)
    }
  }
}