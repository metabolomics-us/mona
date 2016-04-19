package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.service

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus.EventBus
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.config.Notification
import edu.ucdavis.fiehnlab.mona.backend.core.domain.event.Event
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.repository.WebHookRepository
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.types.{WebHook, WebHookResult}
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
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
  def trigger(id: String, eventType:String): Array[WebHookResult] = {

    logger.info(s"triggering all event hooks for id: ${id}")

    val hooks = webhookRepository.findAll().asScala

    if(hooks.isEmpty){
      logger.info("no event hooks provided in the system!")
      Array[WebHookResult]()
    }
    else {
      hooks.collect {

        case hook: WebHook =>
          val url = s"${hook.url}${id}-${eventType}"

          logger.info(s"triggering event: ${url}")

          try {
            restTemplate.getForObject(url, classOf[String])

            val result = WebHookResult(hook.name, url)

            notifications.sendEvent(Event(Notification(result, getClass.getName)))
            result
          }
          catch {
            case x: Throwable =>
              logger.debug(x.getMessage, x)
              val result = WebHookResult(hook.name, url, false, x.getMessage)
              notifications.sendEvent(Event(Notification(result, getClass.getName)))
              result
            case _ => throw new RuntimeException("this should never have happened, something is odd in the webhook service!")
          }

        case x:Any =>
          throw new RuntimeException(s"sorry element ${x} was not valid!")
      }.toArray
    }
  }
}
