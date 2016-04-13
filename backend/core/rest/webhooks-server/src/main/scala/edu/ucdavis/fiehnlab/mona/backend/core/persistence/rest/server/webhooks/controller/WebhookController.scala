package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.controller

import java.util.concurrent.Future

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.GenericRESTController
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.repository.WebHookRepository
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.service.WebHookService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.types.{WebHook, WebHookResult}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.scheduling.annotation.{Async, AsyncResult}
import org.springframework.web.bind.annotation.{PathVariable, RequestMapping, RequestMethod, RestController}

/**
  * this is used to synchronize several webhooks with each other
  */
@RestController
@RequestMapping(Array("/rest/webhooks"))
class WebhookController extends GenericRESTController[WebHook] with LazyLogging{

  @Autowired
  val webhookRepository: WebHookRepository = null

  @Autowired
  val webHookService:WebHookService = null
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
  @RequestMapping(path = Array("/trigger/{id}"), method = Array(RequestMethod.GET))
  @Async
  def triggerHooksForSpectrumId(@PathVariable("id") id: String): Future[Array[WebHookResult]] = {

    logger.info(s"triggering event hooks for spectra: ${id}")
    //trigger all our webhooks
    new AsyncResult[Array[WebHookResult]](
      webHookService.trigger(id)
    )
  }
}
