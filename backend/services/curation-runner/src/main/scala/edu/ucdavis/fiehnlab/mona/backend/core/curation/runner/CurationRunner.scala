package edu.ucdavis.fiehnlab.mona.backend.core.curation.runner

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.listener.GenericMessageListener
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{MetaData, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.EurekaClientConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.api.{ClassificationCacheRestClient, MonaSpectrumRestClient}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.config.RestClientConfig
import edu.ucdavis.fiehnlab.mona.backend.curation.config.{ClassyfireQueueConfig, CurationConfig}
import edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound.classyfire.{ClassyfireProcessor, ClassyfireStats}
import edu.ucdavis.fiehnlab.mona.backend.curation.writer.RestRepositoryWriter
import net.logstash.logback.encoder.org.apache.commons.lang.exception.ExceptionUtils
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.rabbit.listener.{ListenerContainerIdleEvent, SimpleMessageListenerContainer}
import org.springframework.batch.item.ItemProcessor
import org.springframework.beans.factory.annotation.{Autowired, Qualifier, Value}
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.{Bean, Import}
import org.springframework.context.event.EventListener
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.{EnableWebSecurity, WebSecurityConfigurerAdapter}
import org.springframework.web.bind.annotation.{GetMapping, RequestMapping, RestController}
import org.springframework.web.client.HttpClientErrorException

import javax.annotation.PostConstruct
import scala.collection.mutable.Buffer
import scala.jdk.CollectionConverters._

/**
  * This class starts the curation service and let's it listen in the background for messages
  * it also exposes a couple of rest points, which allow simple scheduling of messages.
  *
  * Two queues are consumed:
  *   - curation-queue: the main curation workflow, which no longer classifies compounds
  *   - classyfire-queue: a dedicated single consumer that runs ClassyFire classification, so the slow,
  *     rate limited external service cannot bottleneck curation
  */
@SpringBootApplication
@EnableWebSecurity
@Order(1)
@Import(Array(classOf[RestClientConfig], classOf[CurationConfig], classOf[ClassyfireQueueConfig], classOf[EurekaClientConfig]))
class CurationRunner extends WebSecurityConfigurerAdapter with LazyLogging {

  @Autowired
  @Qualifier("spectra-curation-queue")
  val queueName: String = null

  @Autowired
  @Qualifier("classyfire-queue")
  val classyfireQueueName: String = null

  @Autowired
  @Qualifier("classyfire-pending-queue")
  val classyfirePendingQueueName: String = null

  @Value("${mona.security.curation.token}")
  val token: String = null

  // How long the classyfire queue must be idle before we log a batch summary. Kept well above the pending
  // queue TTL (mona.classyfire.pending.ttl, default 60000) so a pending poll redelivery resets the idle
  // timer first and we never declare a batch done while a scheduled query is still being polled
  @Value("${mona.classyfire.idle.summary.interval:150000}")
  val classyfireIdleSummaryInterval: Long = 150000

  @Autowired
  val rabbitTemplate: RabbitTemplate = null

  @Bean
  def restWriter: RestRepositoryWriter = {
    new RestRepositoryWriter(token)
  }

  @Bean
  def curationListener(curationWorkflow: ItemProcessor[Spectrum, Spectrum], classyfireStats: ClassyfireStats): CurationListener = {
    new CurationListener(curationWorkflow, restWriter, rabbitTemplate, classyfireQueueName, classyfireStats)
  }

  @Bean
  def classyfireListener(classyfireProcessor: ClassyfireProcessor,
                         spectrumClient: MonaSpectrumRestClient,
                         cacheClient: ClassificationCacheRestClient,
                         classyfireStats: ClassyfireStats): ClassyfireListener = {
    new ClassyfireListener(classyfireProcessor, spectrumClient, cacheClient, rabbitTemplate, classyfirePendingQueueName, classyfireStats, token)
  }

  /**
    * Consumes the main curation queue. Concurrency is left at the default of one consumer for now,
    * parallelizing curation (container.setConcurrentConsumers) is a planned follow-up
    *
    * @param connectionFactory
    * @param listener
    * @return
    */
  @Bean
  def container(connectionFactory: ConnectionFactory, listener: CurationListener): SimpleMessageListenerContainer = {
    val container = new SimpleMessageListenerContainer()
    container.setConnectionFactory(connectionFactory)
    container.setMessageListener(listener)
    container.setQueueNames(queueName)
    container
  }

  /**
    * Consumes the dedicated classyfire queue with a single consumer, which serializes all outbound ClassyFire
    * traffic and acts as the global rate limiter. It must never run more than one consumer. An idle interval
    * is set so a batch summary is logged once the queue drains
    *
    * @param connectionFactory
    * @param listener
    * @return
    */
  @Bean
  def classyfireContainer(connectionFactory: ConnectionFactory, listener: ClassyfireListener): SimpleMessageListenerContainer = {
    val container = new SimpleMessageListenerContainer()
    container.setConnectionFactory(connectionFactory)
    container.setMessageListener(listener)
    container.setQueueNames(classyfireQueueName)
    container.setConcurrentConsumers(1)
    container.setMaxConcurrentConsumers(1)
    container.setPrefetchCount(1)
    container.setListenerId(ClassyfireListener.LISTENER_ID)
    container.setIdleEventInterval(classyfireIdleSummaryInterval)
    container
  }

  /**
    * The queue depth gauge is read only and safe to expose without authentication
    *
    * @param web
    */
  override def configure(web: WebSecurity): Unit = {
    web.ignoring().antMatchers(HttpMethod.GET, "/rest/classyfire/**")
  }
}

/**
  * our local server, which should be connecting to eureka, etc
  */
object CurationRunner extends App {
  new SpringApplication(classOf[CurationRunner]).run()
}

/**
  * listens to the curation queue, runs the workflow, saves the result, then hands the spectrum off to the
  * dedicated classyfire queue for background classification
  */
class CurationListener(workflow: ItemProcessor[Spectrum, Spectrum],
                       writer: RestRepositoryWriter,
                       rabbitTemplate: RabbitTemplate,
                       classyfireQueueName: String,
                       stats: ClassyfireStats) extends GenericMessageListener[Spectrum] with LazyLogging {

  override def handleMessage(spectrum: Spectrum): Unit = {
    try {
      logger.info(s"${spectrum.getId}: Received spectrum")
      val result: Spectrum = workflow.process(spectrum)
      logger.info(s"${spectrum.getId}: Finished curating spectrum")
      writer.write(result)
      logger.info(s"${spectrum.getId}: Saved spectrum to system")

      // Hand off to the dedicated classyfire queue, tracking the live queue depth
      rabbitTemplate.convertAndSend(classyfireQueueName, result)
      stats.incQueued()
    } catch {
      case e: Exception =>
        logger.info(s"${spectrum.getId}: Exception occurred during curation, fail silently: ${e.getMessage}")
        logger.info(s"${spectrum.getId}: ${ExceptionUtils.getStackTrace(e)}")
    }
  }
}

/**
  * Single consumer listener that classifies a spectrum's compounds with ClassyFire. When a classification was
  * scheduled but is not finished, the spectrum is re-enqueued to the pending queue to be polled later;
  * otherwise the freshly fetched spectrum is updated with the classification and saved
  */
class ClassyfireListener(classyfireProcessor: ClassyfireProcessor,
                         spectrumClient: MonaSpectrumRestClient,
                         cacheClient: ClassificationCacheRestClient,
                         rabbitTemplate: RabbitTemplate,
                         pendingQueueName: String,
                         stats: ClassyfireStats,
                         token: String) extends GenericMessageListener[Spectrum] with LazyLogging {

  @PostConstruct
  def authorize(): Unit = {
    spectrumClient.login(token)
    if (cacheClient != null) {
      cacheClient.login(token)
    }
  }

  override def handleMessage(spectrum: Spectrum): Unit = {
    stats.incSpectraReceived()

    try {
      val result: Spectrum = classyfireProcessor.process(spectrum)

      // Re-enqueue when a query is still being computed, or when ClassyFire was unreachable, so the work is
      // retried later rather than dropped. Either way it stays outstanding, so the queue depth is not decremented
      if (classyfireProcessor.hasPendingClassification(result) || !classyfireProcessor.serviceAvailable) {
        logger.info(s"${result.getId}: ClassyFire classification not yet complete, re-enqueueing to retry later")
        rabbitTemplate.convertAndSend(pendingQueueName, result)
        stats.incPending()
      } else {
        persistClassification(result)
        stats.decQueued()
      }
    } catch {
      case e: Exception =>
        logger.warn(s"${spectrum.getId}: Classification failed, fail silently: ${e.getMessage}")
        // Treat as terminal so the live queue depth cannot leak
        stats.decQueued()
    }
  }

  /**
    * Fetch the current spectrum and apply only the classification, so a curation that ran while this spectrum
    * waited in the queue is not clobbered. The classification is additive, the rest of the spectrum is left as
    * stored. A spectrum that was deleted in the meantime is skipped
    *
    * @param classified
    */
  private def persistClassification(classified: Spectrum): Unit = {
    try {
      val fresh: Spectrum = spectrumClient.get(classified.getId)
      val changed: Boolean = applyClassification(fresh, classified)

      if (!changed) {
        // Nothing new to store (the compound was already classified), so skip the write entirely. This avoids
        // a pointless update and, importantly, avoids re-emitting a spectrum event that would re-trigger curation
        logger.info(s"${classified.getId}: classification unchanged, nothing to persist")
      } else {
        // Carry over the curation timestamp from the just curated spectrum. Without this the persisted update
        // would keep the stale stored lastCurated, and the curation event bus listener would treat it as due
        // for curation again, re-curating this spectrum in an endless loop
        if (classified.getLastCurated != null) {
          fresh.setLastCurated(classified.getLastCurated)
        }
        spectrumClient.updateAsync(fresh, fresh.getId)
        logger.info(s"${classified.getId}: classification persisted")
      }
    } catch {
      case e: HttpClientErrorException if e.getStatusCode.value() == 404 =>
        logger.info(s"${classified.getId}: spectrum no longer exists, skipping classification persist")
    }
  }

  /**
    * Copy each classified compound's classification onto the matching compound of the current spectrum,
    * matched by kind and InChIKey, falling back to kind alone. Entries are copied fresh so they insert cleanly.
    * Returns true if any compound's classification actually changed, so the caller can skip a redundant save
    *
    * @param target the current spectrum to update
    * @param source the freshly classified spectrum
    * @return whether anything changed
    */
  private def applyClassification(target: Spectrum, source: Spectrum): Boolean = {
    if (target.getCompound == null || source.getCompound == null) {
      return false
    }

    val sourceCompounds = source.getCompound.asScala
    var changed = false

    target.getCompound.asScala.foreach { targetCompound =>
      sourceCompounds.find(s => s.getKind == targetCompound.getKind && s.getInchiKey == targetCompound.getInchiKey)
        .orElse(sourceCompounds.find(_.getKind == targetCompound.getKind))
        .foreach { matched =>
          val newClassification = Option(matched.getClassification).map(_.asScala).getOrElse(Buffer.empty[MetaData])
          val current = Option(targetCompound.getClassification).map(_.asScala).getOrElse(Buffer.empty[MetaData])

          if (newClassification.nonEmpty && !sameClassification(current, newClassification)) {
            targetCompound.setClassification(newClassification.map(new MetaData(_)).asJava)
            changed = true
          }
        }
    }

    changed
  }

  /**
    * Compare two classification lists by name and value, ignoring order, so an unchanged classification is not
    * needlessly rewritten
    *
    * @param a
    * @param b
    * @return
    */
  private def sameClassification(a: Buffer[MetaData], b: Buffer[MetaData]): Boolean = {
    def keys(metaData: Buffer[MetaData]): List[(String, String)] =
      metaData.map(m => (Option(m.getName).getOrElse(""), Option(m.getValue).getOrElse(""))).sortBy(identity).toList
    keys(a) == keys(b)
  }

  /**
    * When the classyfire queue has drained, log a one line summary of the batch and reset the counters
    *
    * @param event
    */
  @EventListener
  def onContainerIdle(event: ListenerContainerIdleEvent): Unit = {
    if (event.getListenerId == ClassyfireListener.LISTENER_ID && stats.hasActivity) {
      logger.info(stats.summary)
      stats.reset()
    }
  }
}

object ClassyfireListener {
  val LISTENER_ID: String = "classyfire-listener"
}

/**
  * Exposes the live ClassyFire queue depth so other services can tell whether classification is still
  * draining, for example to guard the re-curation action against being triggered again too soon
  */
@RestController
@RequestMapping(Array("/rest/classyfire"))
class ClassyfireQueueController {

  @Autowired
  val classyfireStats: ClassyfireStats = null

  @GetMapping(Array("/queueDepth"))
  def queueDepth: Long = classyfireStats.currentQueueDepth
}
