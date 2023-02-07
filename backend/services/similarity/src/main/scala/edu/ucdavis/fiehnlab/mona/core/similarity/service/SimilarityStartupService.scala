package edu.ucdavis.fiehnlab.mona.core.similarity.service

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.core.similarity.util.IndexUtils
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import scala.jdk.CollectionConverters._
import scala.collection.mutable.Buffer

/**
  * Created by sajjan on 1/3/17.
  */
@Service
class SimilarityStartupService extends ApplicationListener[ApplicationReadyEvent] with LazyLogging {

  @Autowired
  val indexUtils: IndexUtils = null

  @Autowired
  val similarityPopulationService: SimilarityPopulationService = null

  @Value("${mona.similarity.autopopulate:true}")
  private val autoPopulate: Boolean = true

  @Transactional()
  override def onApplicationEvent(e: ApplicationReadyEvent): Unit = {
    if (autoPopulate) {
      logger.info("Starting auto-population of indices")
      similarityPopulationService.populateIndices()
    }
  }
}
