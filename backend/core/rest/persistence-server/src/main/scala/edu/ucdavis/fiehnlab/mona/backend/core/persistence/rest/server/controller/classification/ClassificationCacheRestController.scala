package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.classification

import edu.ucdavis.fiehnlab.mona.backend.core.domain.ClassificationCache
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.ClassificationCacheRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.scheduling.annotation.{Async, AsyncResult}
import org.springframework.web.bind.annotation._

import java.util.concurrent.Future

/**
  * Exposes the per skeleton ClassyFire classification cache so the REST only curation runner can read and write
  * it. Keyed by the 14 character InChIKey first block. Only a lookup and a save are needed
  */
@CrossOrigin
@RestController
@RequestMapping(Array("/rest/classificationCache"))
@Profile(Array("mona.persistence"))
class ClassificationCacheRestController {

  @Autowired
  val classificationCacheRepository: ClassificationCacheRepository = null

  /**
    * Returns the cached classification for the given InChIKey block, or 404 if it has not been classified yet
    *
    * @param id the 14 character InChIKey first block
    * @return
    */
  @Async
  @RequestMapping(path = Array("/{id}"), method = Array(RequestMethod.GET))
  @ResponseBody
  final def get(@PathVariable("id") id: String): Future[ResponseEntity[ClassificationCache]] = {
    if (classificationCacheRepository.existsById(id)) {
      new AsyncResult[ResponseEntity[ClassificationCache]](new ResponseEntity[ClassificationCache](classificationCacheRepository.findById(id).orElse(null), HttpStatus.OK))
    } else {
      new AsyncResult[ResponseEntity[ClassificationCache]](new ResponseEntity[ClassificationCache](HttpStatus.NOT_FOUND))
    }
  }

  /**
    * Stores or replaces a cached classification
    *
    * @param resource
    * @return
    */
  @Async
  @RequestMapping(path = Array(""), method = Array(RequestMethod.POST))
  @ResponseBody
  final def save(@RequestBody resource: ClassificationCache): Future[ResponseEntity[ClassificationCache]] = {
    new AsyncResult[ResponseEntity[ClassificationCache]](new ResponseEntity[ClassificationCache](classificationCacheRepository.save(resource), HttpStatus.OK))
  }
}
