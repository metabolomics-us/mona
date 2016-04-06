package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.{RequestMapping, RestController}

/**
  * this is used to synchronize several webhooks with each other
  */
@RestController
@RequestMapping(Array("/rest/webhooks"))
class WebhookController extends GenericRESTController[WebHook]{

  @Autowired
  val webhookRepository:PagingAndSortingRepository[WebHook,String] = null

  /**
    * utilized repository
    *
    * @return
    */
  override def getRepository: PagingAndSortingRepository[WebHook, String] = webhookRepository
}
