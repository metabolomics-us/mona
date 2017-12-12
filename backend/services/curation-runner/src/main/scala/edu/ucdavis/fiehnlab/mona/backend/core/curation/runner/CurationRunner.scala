package edu.ucdavis.fiehnlab.mona.backend.core.curation.runner

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.listener.GenericMessageListener
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.EurekaClientConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.config.RestClientConfig
import edu.ucdavis.fiehnlab.mona.backend.curation.config.CurationConfig
import edu.ucdavis.fiehnlab.mona.backend.curation.writer.RestRepositoryWriter
import net.logstash.logback.encoder.org.apache.commons.lang.exception.ExceptionUtils
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.batch.item.ItemProcessor
import org.springframework.beans.factory.annotation.{Autowired, Qualifier, Value}
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.{Bean, Import}
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.configuration.{EnableWebSecurity, WebSecurityConfigurerAdapter}

/**
  * This class starts the curation service and let's it listen in the background for messages
  * it also exposes a couple of rest points, which allow simple scheduling of messages
  */
@SpringBootApplication
@EnableWebSecurity
@Order(1)
@Import(Array(classOf[RestClientConfig], classOf[CurationConfig], classOf[EurekaClientConfig]))
class CurationRunner extends WebSecurityConfigurerAdapter with LazyLogging {

  @Autowired
  @Qualifier("spectra-curation-queue")
  val queueName: String = null

  @Value("${mona.security.curation.token}")
  val token: String = null

  @Bean
  def restWriter: RestRepositoryWriter = {
    new RestRepositoryWriter(token)
  }

  @Bean
  def curationListener(curationWorkflow: ItemProcessor[Spectrum, Spectrum]): CurationListener = {
    new CurationListener(curationWorkflow, restWriter)
  }

  @Bean
  def container(connectionFactory: ConnectionFactory, listener: CurationListener, messageConverter: MessageConverter): SimpleMessageListenerContainer = {
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
class CurationListener(workflow: ItemProcessor[Spectrum, Spectrum], writer: RestRepositoryWriter) extends GenericMessageListener[Spectrum] with LazyLogging {

  override def handleMessage(spectrum: Spectrum): Unit = {
    try {
      logger.info(s"Received spectrum: ${spectrum.id}")
      val result: Spectrum = workflow.process(spectrum)
      logger.info(s"Finished curating spectrum: ${spectrum.id}")
      writer.write(result)
      logger.info(s"Saved spectrum ${spectrum.id} to system")
    } catch {
      case e: Exception =>
        logger.info(s"Exception occurred during curation of spectrum ${spectrum.id}, fail silently: ${e.getMessage}")
        logger.info(ExceptionUtils.getStackTrace(e))
    }
  }
}