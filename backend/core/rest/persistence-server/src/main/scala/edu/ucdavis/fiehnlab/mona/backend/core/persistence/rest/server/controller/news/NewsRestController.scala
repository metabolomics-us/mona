package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.news

import edu.ucdavis.fiehnlab.mona.backend.core.domain.NewsEntry
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.NewsMongoRepository
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.GenericRESTController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.web.bind.annotation._

/**
  * Created by wohlg_000 on 3/7/2016.
  */
@CrossOrigin
@RestController
@RequestMapping(Array("/rest/news"))
class NewsRestController extends GenericRESTController[NewsEntry] {

  @Autowired
  val newsRepository: NewsMongoRepository = null

  /**
    * Utilized repository
    *
    * @return
    */
  override def getRepository: PagingAndSortingRepository[NewsEntry, String] = newsRepository
}