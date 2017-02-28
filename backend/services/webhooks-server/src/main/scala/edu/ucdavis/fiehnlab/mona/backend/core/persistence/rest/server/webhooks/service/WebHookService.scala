package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.service

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus.EventBus
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.config.Notification
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.event.Event
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.api.MonaSpectrumRestClient
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.repository.{WebHookRepository, WebHookResultRepository}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.types.{WebHook, WebHookResult}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.service.persistence.SpectrumPersistenceService
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.web.client.{HttpClientErrorException, RestTemplate}

import scala.collection.JavaConverters._

/**
  * a simple service to ensure we can trigger the internally defined webhooks
  */
@Service
class WebHookService extends LazyLogging {

  @Autowired
  val webHookResultRepository: WebHookResultRepository = null

  @Autowired
  val webhookRepository: WebHookRepository = null

  val restTemplate: RestTemplate = new RestTemplate()

  @Autowired
  val notifications: EventBus[Notification] = null

  /**
    * our connection to the local repository
    */
  @Autowired
  val spectrumPersistenceService: SpectrumPersistenceService = null


  /**
    * our remote connection to the main mona server, as configured in our application properties
    */
  @Autowired
  val monaSpectrumRestClient: MonaSpectrumRestClient = null

  /**
    * triggers a invocation of all the webhooks in the system based on the given id
    *
    * @param id
    */
  def trigger(id: String, eventType: String): Array[WebHookResult] = {

    logger.debug(s"triggering all event hooks for id: $id")

    val hooks = webhookRepository.findAll().asScala

    if (hooks.isEmpty) {
      logger.debug("no event hooks provided in the system!")
      Array[WebHookResult]()
    } else {
      hooks.map { hook: WebHook =>

        val url = s"${hook.url}?id=$id&type=$eventType"

        logger.debug(s"triggering event: $url")

        try {
          restTemplate.getForObject(url, classOf[String])

          val result = webHookResultRepository.save(WebHookResult(ObjectId.get().toHexString, hook.name, url))
          notifications.sendEvent(Event(Notification(result, getClass.getName)))

          result
        } catch {
          case x: Throwable =>
            logger.debug(x.getMessage, x)
            val result = webHookResultRepository.save(WebHookResult(ObjectId.get().toHexString, hook.name, url, success = false, x.getMessage))
            notifications.sendEvent(Event(Notification(result, getClass.getName)))

            result
        }

      }.toArray
    }
  }

  /**
    * synchronizes the given event type against the configured master mona server
    *
    * @param id
    * @param eventType
    * @return
    */
  def sync(id: String, eventType: String): ResponseEntity[Any] = {

    try {
      eventType.toLowerCase match {

        case Event.ADD =>
          logger.info("adding spectra")

          logger.info(s"fetching spectrum from remote mona server for id: $id")
          val spectrum: Spectrum = monaSpectrumRestClient.get(id)

          val result = spectrumPersistenceService.save(spectrum)

          logger.info(s"internal spectra is: $result")
          new ResponseEntity[Any](result, HttpStatus.OK)
        case Event.DELETE =>
          logger.info("deleting spectra")

          //make sure spectra doesn't exist in remote first
          try {
            monaSpectrumRestClient.get(id)

            //throw an error, since we can't delete a spectra which officially exists on the remote server side
            new ResponseEntity[Any](s"sorry this spectra ($id) does still exist on the remote server", HttpStatus.NOT_FOUND)
          }
          catch {
            case e: HttpClientErrorException =>
              if (e.getMessage == "404 Not Found") {
                //spectra does not exit, now we can delete it safely
                val spectrum = spectrumPersistenceService.findOne(id)

                if (spectrum != null) {
                  spectrumPersistenceService.delete(id)
                  new ResponseEntity[Any](HttpStatus.OK)
                }
                else {
                  new ResponseEntity[Any](s"sorry this spectra ($id) did not exist on the local server", HttpStatus.NOT_FOUND)
                }
              }
              else {
                new ResponseEntity[Any](e.getMessage, HttpStatus.BAD_REQUEST)
              }
          }

        case Event.UPDATE =>
          logger.info("updating spectra")

          logger.info(s"fetching spectrum from remote mona server for id: $id")
          val spectrum: Spectrum = monaSpectrumRestClient.get(id)
          val result = spectrumPersistenceService.update(spectrum)

          new ResponseEntity[Any](result, HttpStatus.OK)
        case _ =>
          new ResponseEntity[Any](s"invalid request, event must match ${Event.ADD}/${Event.DELETE}/${Event.UPDATE}", HttpStatus.BAD_REQUEST)
      }
    }
    catch {
      case e: HttpClientErrorException =>
        new ResponseEntity[Any](s"spectrum with $id was not found on origin server: ${e.getMessage}", HttpStatus.NOT_FOUND)
    }
  }

  /**
    * pulls a copy from the remote mona service. Optionally allows you to specify a query
    */
  @Async
  def pull(query: Option[String] = None): Unit = {
    val count = monaSpectrumRestClient.count(query)
    logger.info(s"expected spectra to pull: $count")
    var counter = 0

    monaSpectrumRestClient.stream(query).foreach { spectrum: Spectrum =>
      counter = counter + 1
      logger.info(s"spectrum: ${spectrum.id} - ${spectrum.splash}")
      spectrumPersistenceService.save(spectrum)
    }

    logger.info(s"retrieved $counter spectra from master")
  }

  /**
    * pushes all spectra as update to all slaves of this server
    *
    * @param query
    */
  @Async
  def push(query: Option[String] = None): Unit = {
    //should send it as job to the backend
    spectrumPersistenceService.findAll().asScala.foreach { spectrum =>
      trigger(spectrum.id, Event.UPDATE)
    }
  }
}
