package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.repository

import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.domain.WebHook
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
  * Created by wohlgemuth on 4/5/16.
  */
@Repository
@Profile(Array("mona.persistence"))
trait WebHookRepository extends JpaRepository[WebHook, String] {

  /**
    * returns the webhooks associated with the given account
    *
    * @param emailAddress
    * @return
    */
  def findByEmailAddress(emailAddress: String): Array[WebHook]
}
