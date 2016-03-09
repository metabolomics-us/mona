package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.controller

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Submitter
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.web.bind.annotation.{RequestMapping, RestController}

/**
  * Created by wohlgemuth on 3/7/16.
  */
@RestController
@RequestMapping(Array("/rest/submitter"))
class SubmitterRestController extends GenericRESTController[Submitter]{
  /**
    * utilized repository
    *
    * @return
    */
  override def getRepository: PagingAndSortingRepository[Submitter, String] = ???
}
