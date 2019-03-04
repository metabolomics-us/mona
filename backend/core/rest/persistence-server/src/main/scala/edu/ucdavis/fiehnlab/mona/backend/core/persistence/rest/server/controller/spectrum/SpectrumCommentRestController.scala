package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.spectrum

import edu.ucdavis.fiehnlab.mona.backend.core.domain.SpectrumComment
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.SpectrumCommentMongoRepository
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.GenericRESTController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.web.bind.annotation._

/**
  * Created by sajjan on 06/19/18.
  */
@CrossOrigin
@RestController
@RequestMapping(Array("/rest/comments"))
class SpectrumCommentRestController extends GenericRESTController[SpectrumComment] {

  @Autowired
  val commentRepository: SpectrumCommentMongoRepository = null

  /**
    * Utilized repository
    *
    * @return
    */
  override def getRepository: PagingAndSortingRepository[SpectrumComment, String] = commentRepository
}