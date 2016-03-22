package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.submitter

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Submitter
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.ISubmitterMongoRepository
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.GenericRESTController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.web.bind.annotation.{RequestMapping, RestController}

/**
  * Created by wohlgemuth on 3/7/16.
  */
@RestController
@RequestMapping(Array("/rest/submitters"))
class SubmitterRestController extends GenericRESTController[Submitter] {

  /**
    * this is the utilized repository, doing all the heavy lifting
    */
  @Autowired
  val submitterMongoRepository: ISubmitterMongoRepository = null

  /**
    * utilized repository
    *
    * @return
    */
  override def getRepository: PagingAndSortingRepository[Submitter, String] = submitterMongoRepository
}
