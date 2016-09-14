package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.metadata

import java.util
import java.util.concurrent.Future

import com.mongodb.DBObject
import edu.ucdavis.fiehnlab.mona.backend.core.domain.HelperTypes.WrappedString
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.ISpectrumMongoRepositoryCustom
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.repository.MetaDataStatisticsMongoRepository
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.service.StatisticsService
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.types.MetaDataStatistics
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.aggregation.Aggregation._
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.scheduling.annotation.{Async, AsyncResult}
import org.springframework.web.bind.annotation._

import scala.collection.JavaConverters._

/**
  * Created by wohlg_000 on 3/7/2016.
  */
@CrossOrigin
@RestController
@RequestMapping(Array("/rest/metaData"))
class MetaDataRestController {

  @Autowired
  val mongoOperations: MongoOperations = null

  @Autowired
  val statisticsService: StatisticsService = null


  /**
    * List unique metadata names
    * @return
    */
  @RequestMapping(path = Array("/names"), method = Array(RequestMethod.GET))
  @Async
  def listMetaDataNames: Future[Array[String]] = new AsyncResult[Array[String]](statisticsService.getMetaDataNames)

  /**
    *
    * @param partialMetaDataName
    * @return
    */
  @RequestMapping(path = Array("/names/search"), method = Array(RequestMethod.POST))
  @Async
  def searchMetaDataName(@RequestBody partialMetaDataName: WrappedString): Future[java.util.List[String]] = {
    null
  }

  /**
    *
    * @param metaDataName
    * @return
    */
  @RequestMapping(path = Array("/values"), method = Array(RequestMethod.POST))
  @Async
  def listMetaDataValue(@RequestBody metaDataName: WrappedString): AsyncResult[MetaDataStatistics] =
    new AsyncResult[MetaDataStatistics](statisticsService.getMetaDataStatistics(metaDataName.string))

  /**
    *
    * @param metaDataName
    * @param partialMetaDataValue
    * @return
    */
  @RequestMapping(path = Array("/values/search"), method = Array(RequestMethod.GET))
  @Async
  def searchMetaDataValues(@RequestBody metaDataName: WrappedString, @RequestBody partialMetaDataValue: WrappedString): Future[java.util.List[String]] = {
    null
  }
}
