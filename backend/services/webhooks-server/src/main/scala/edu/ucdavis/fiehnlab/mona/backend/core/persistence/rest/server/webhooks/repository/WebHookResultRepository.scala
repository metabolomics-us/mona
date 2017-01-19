package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.repository

import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.types.WebHookResult
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

/**
  * Created by wohlgemuth on 1/13/17.
  */
@Repository
trait WebHookResultRepository extends PagingAndSortingRepository[WebHookResult, String] {

}
