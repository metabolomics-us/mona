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
import org.springframework.web.bind.annotation.{CrossOrigin, RequestMapping, RequestMethod, RestController}

import scala.collection.JavaConverters._

/**
  * Created by sajjan on 8/4/16.
  */
@CrossOrigin
@RestController
@RequestMapping(Array("/rest"))
class StatisticsRestController {

  @Autowired
  val mongoOperations: MongoOperations = null

  @Autowired
  val spectrumRepository: ISpectrumMongoRepositoryCustom = null

  val statisticsService: StatisticsService = new StatisticsService


  @RequestMapping(path = Array("/statistics/tags"), method = Array(RequestMethod.GET))
  @Async
  def listTags: Future[Iterable[TagStatistics]] = new AsyncResult[Iterable[TagStatistics]](statisticsService.getTagStatistics)

  @RequestMapping(path = Array("/statistics/metaData"), method = Array(RequestMethod.GET))
  @Async
  def listMetaData: Future[Iterable[MetaDataStatistics]] = new AsyncResult[Iterable[MetaDataStatistics]](statisticsService.getMetaDataStatistics)

  
  @RequestMapping(path = Array("/statistics/update"), method = Array(RequestMethod.GET))
  @Async
  def updateStatistics() = statisticsService.updateStatistics()
}
