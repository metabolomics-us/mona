package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.statistics

import java.util
import java.util.concurrent.Future

import com.mongodb.DBObject
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.ISpectrumMongoRepositoryCustom
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.repository.{MetaDataStatisticsMongoRepository, TagStatisticsMongoRepository}
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.service.StatisticsService
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.types.{MetaDataStatistics, TagStatistics}
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.aggregation.Aggregation._
import org.springframework.scheduling.annotation.{Async, AsyncResult}
import org.springframework.web.bind.annotation._

import scala.collection.JavaConverters._

/**
  * Created by sajjan on 8/4/16.
  */
@CrossOrigin
@RestController
@RequestMapping(Array("/rest"))
class StatisticsRestController {

  @Autowired
  val statisticsService: StatisticsService = null


  /**
    * Get a list of unique tags and their respective counts
    * @return
    */
  @RequestMapping(path = Array("/tags"), method = Array(RequestMethod.GET))
  @Async
  def listTags: Future[Iterable[TagStatistics]] = new AsyncResult[Iterable[TagStatistics]](statisticsService.getTagStatistics.asScala)

  /**
    * Get all metadata statistics
    * @return
    */
  @RequestMapping(path = Array("/statistics/metaData"), method = Array(RequestMethod.GET))
  @Async
  def listMetaData: Future[Iterable[MetaDataStatistics]] = new AsyncResult[Iterable[MetaDataStatistics]](statisticsService.getMetaDataStatistics.asScala)

  /**
    * Update statistics
    * @return
    */
  @RequestMapping(path = Array("/statistics/update"), method = Array(RequestMethod.POST))
  @Async
  @ResponseBody
  def updateStatistics(): String = {
    statisticsService.updateStatistics()

    "Statistics update queued"
  }
}