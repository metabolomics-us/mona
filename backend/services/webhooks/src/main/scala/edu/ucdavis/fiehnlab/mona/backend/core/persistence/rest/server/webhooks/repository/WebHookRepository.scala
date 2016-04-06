package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.repository

import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.types.WebHook
import org.springframework.data.repository.{PagingAndSortingRepository, Repository}

/**
  * Created by wohlgemuth on 4/5/16.
  */
@Repository
trait WebHookRepository extends PagingAndSortingRepository[WebHook, String]{

}
