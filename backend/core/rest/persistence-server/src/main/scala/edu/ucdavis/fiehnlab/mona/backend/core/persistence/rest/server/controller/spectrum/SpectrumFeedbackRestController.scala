package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.spectrum

import edu.ucdavis.fiehnlab.mona.backend.core.domain.SpectrumFeedback
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.SpectrumFeedbackMongoRepository
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.GenericRESTController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.web.bind.annotation._

/**
  * Created by sajjan on 06/19/18.
  */
@CrossOrigin
@RestController
@RequestMapping(Array("/rest/feedback"))
class SpectrumFeedbackRestController extends GenericRESTController[SpectrumFeedback] {

  @Autowired
  val commentRepository: SpectrumFeedbackMongoRepository = null

  /**
    * Utilized repository
    *
    * @return
    */
  override def getRepository: PagingAndSortingRepository[SpectrumFeedback, String] = commentRepository
}