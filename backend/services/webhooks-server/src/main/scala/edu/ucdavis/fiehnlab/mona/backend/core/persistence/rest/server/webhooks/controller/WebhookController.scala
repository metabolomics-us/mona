package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.controller

import java.util.concurrent.Future

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.GenericRESTController
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.repository.WebHookRepository
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.service.WebHookService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.types.{WebHook, WebHookResult}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.http.ResponseEntity
import org.springframework.scheduling.annotation.{Async, AsyncResult}
import org.springframework.web.bind.annotation._

/**
  * this is used to synchronize several webhooks with each other
  */
@RestController
@RequestMapping(Array("/rest/webhooks"))
class WebhookController extends GenericRESTController[WebHook] with LazyLogging {

  @Autowired
  val webhookRepository: WebHookRepository = null

  @Autowired
  val webHookService: WebHookService = null

  /**
    *
    * utilized repository
    *
    * @return
    */
  override def getRepository: PagingAndSortingRepository[WebHook, String] = webhookRepository


  /**
    * this
    *
    * @param id
    */
  @RequestMapping(path = Array("/trigger/{id}/{type}"), method = Array(RequestMethod.GET))
  def triggerHooksForSpectrumId(@PathVariable("id") id: String, @PathVariable("type") eventType: String): Future[Array[WebHookResult]] = {

    logger.info(s"triggering event hooks for spectra: ${id} and type ${eventType}")
    //trigger all our webhooks
    new AsyncResult[Array[WebHookResult]](
      webHookService.trigger(id, eventType)
    )
  }


  /**
    * pulls all data from the master
    * @param query
    * @return
    */
  @RequestMapping(path = Array("/pull"), method = Array(RequestMethod.POST))
  def pull(@RequestParam(name = "query", required = false, defaultValue = "") query: String) = {
    if (query == "") {
      webHookService.pull()
    }
    else {
      webHookService.pull(Some(query))
    }
  }


  /**
    * pushes all local data, to all it's slaves
    * @param query
    * @return
    */
  @RequestMapping(path = Array("/push"), method = Array(RequestMethod.POST))
  def push(@RequestParam(name = "query", required = false, defaultValue = "") query: String) = {
    if (query == "") {
      webHookService.push()
    }
    else {
      webHookService.push(Some(query))
    }
  }

  /**
    * provides a webhook client from mona to a master mona instance
    *
    * @param id
    * @param eventType
    */
  @RequestMapping(path = Array("/sync"), method = Array(RequestMethod.GET))
  @Async
  def sync(@RequestParam("id") id: String, @RequestParam("type") eventType: String): ResponseEntity[Any] = {
    webHookService.sync(id, eventType)
  }
}
