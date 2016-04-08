package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.service

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus.EventBus
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.config.Notification
import edu.ucdavis.fiehnlab.mona.backend.core.domain.event.Event
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.repository.WebHookRepository
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.types.{WebHook, WebHookResult}
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

import scala.collection.JavaConverters._

/**
  * a simple service to ensure we can trigger the internally defined webhooks
  */
@Service
class WebHookService extends LazyLogging {

  @Autowired
  val webhookRepository: WebHookRepository = null

  val restTemplate: RestTemplate = new RestTemplate()

  @Autowired
  val notifications: EventBus[Notification] = null

  /**
    * triggers a invocation of all the webhooks in the system based on the given id
    *
    * @param id
    */
  def trigger(id: String): Array[WebHookResult] = {

    logger.debug(s"triggering all event hooks for id: ${id}")
    webhookRepository.findAll().asScala.collect {

      case hook: WebHook =>
        val url = s"${hook.url}${id}"

        try {
          restTemplate.getForObject(url, classOf[String])

          val result = WebHookResult(hook.name, url)
          notifications.sendEvent(Event(Notification(result, getClass.getName)))
          result
        }
        catch {
          case x: Exception =>
            logger.debug(x.getMessage, x)
            val result = WebHookResult(hook.name, url, false, x.getMessage)
            notifications.sendEvent(Event(Notification(result, getClass.getName)))
            result
        }

    }.toArray
  }
}
