package edu.ucdavis.fiehnlab.mona.backend.core.statistics.service

import java.lang
import java.util.LinkedHashMap

import com.mongodb.DBObject
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.repository.{MetaDataStatisticsMongoRepository, TagStatisticsMongoRepository}
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.types.{MetaDataStatistics, MetaDataValueCount, TagStatistics}
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.aggregation.Aggregation._
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

import scala.collection.JavaConverters._

/**
  * Created by sajjan on 8/2/16.
  */
@Service
class StatisticsService {

  @Autowired
  val mongoOperations: MongoOperations = null

  @Autowired
  val metaDataStatisticsService: MetaDataStatisticsService = null

  @Autowired
  val tagStatisticsService: TagStatisticsService = null


  /**
    * Update all statistics
    */
  @Async
  def updateStatistics() = {
    metaDataStatisticsService.updateMetaDataStatistics()
    tagStatisticsService.updateTagStatistics()


  }
}
