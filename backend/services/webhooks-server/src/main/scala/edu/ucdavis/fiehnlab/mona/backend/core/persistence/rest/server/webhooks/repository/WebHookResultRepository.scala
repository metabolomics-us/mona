package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.repository

import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.domain.WebHookResult
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
  * Created by wohlgemuth on 1/13/17.
  */
@Repository
@Profile(Array("mona.persistence"))
trait WebHookResultRepository extends JpaRepository[WebHookResult, String] {

}
