package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.repository

import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.types.WebHook
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

/**
  * Created by wohlgemuth on 4/5/16.
  */
@Repository
trait WebHookRepository extends PagingAndSortingRepository[WebHook, String]{

  /**
    * returns the webhooks associated with the given account
    * @param username
    * @return
    */
  def findByUsername(username: String): Array[WebHook]
}
