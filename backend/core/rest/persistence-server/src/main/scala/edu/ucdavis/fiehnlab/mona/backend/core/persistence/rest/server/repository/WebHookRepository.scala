package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.repository

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.service.listener.PersistenceEvent
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

/**
  * Created by wohlgemuth on 4/5/16.
  */
@Repository
trait WebHookRepository extends PagingAndSortingRepository[WebHook, String]{

}

/**
  * defines an internal or external webhook to be notified in case of observed events
  * @param name
  */
@Document(collection = "WEBHOOK")
case class WebHook(
                    id:String,
                    name:String,
                    description:String,
                    url:String
                  )