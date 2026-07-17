package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.api

import edu.ucdavis.fiehnlab.mona.backend.core.domain.ClassificationCache
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException

/**
  * Client for the per skeleton ClassyFire classification cache. The curation runner has no direct database
  * access, so it reaches the cache table through the persistence server
  */
@Component
class ClassificationCacheRestClient extends GenericRestClient[ClassificationCache, String]("rest/classificationCache") {

  /**
    * Looks up a cached classification by its 14 character InChIKey block, returning None when it has not been
    * classified yet (the server answers 404)
    *
    * @param inchikeyBlock
    * @return
    */
  def findByBlock(inchikeyBlock: String): Option[ClassificationCache] = {
    try {
      Option(get(inchikeyBlock))
    } catch {
      case e: HttpClientErrorException if e.getStatusCode.value() == 404 => None
    }
  }
}
