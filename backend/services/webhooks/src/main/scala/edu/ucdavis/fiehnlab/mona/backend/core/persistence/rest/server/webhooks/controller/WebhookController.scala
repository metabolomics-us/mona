package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.controller

import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.GenericRESTController
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.repository.WebHookRepository
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.types.WebHook
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.web.bind.annotation.{RequestMapping, RestController}

/**
  * this is used to synchronize several webhooks with each other
  */
@RestController
@RequestMapping(Array("/rest/webhooks"))
class WebhookController extends GenericRESTController[WebHook]{

  @Autowired
  val webhookRepository:WebHookRepository = null

  /**
    * utilized repository
    *
    * @return
    */
  override def getRepository: PagingAndSortingRepository[WebHook, String] = webhookRepository
}
