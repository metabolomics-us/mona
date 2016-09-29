package edu.ucdavis.fiehnlab.mona.backend.core.statistics.service

import java.lang
import java.util.{Date, LinkedHashMap}

import com.mongodb.DBObject
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.ISpectrumMongoRepositoryCustom
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.repository.{GlobalStatisticsMongoRepository, MetaDataStatisticsMongoRepository, TagStatisticsMongoRepository}
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.types.{GlobalStatistics, MetaDataStatistics, MetaDataValueCount, TagStatistics}
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
  private val mongoOperations: MongoOperations = null

  @Autowired
  private val spectrumMongoRepository: ISpectrumMongoRepositoryCustom = null

  @Autowired
  private val globalStatisticsRepository: GlobalStatisticsMongoRepository = null

  @Autowired
  private val metaDataStatisticsService: MetaDataStatisticsService = null

  @Autowired
  private val tagStatisticsService: TagStatisticsService = null


  /**
    * Update the data in the global statistics repository
    * @return
    */
  def updateGlobalStatistics() = {
    // Spectrum count
    val spectrumCount: Long = spectrumMongoRepository.count()


    // Compound count
    // TODO: Use only first InChIKey block
    val compoundCount: Long =
      mongoOperations.aggregate(
        newAggregation(
          classOf[Spectrum],
          project("compound"),
          unwind("compound"),
          unwind("compound.metaData"),
          `match`(Criteria.where("compound.metaData.name").is("InChIKey")),
          group().count().as("count")
        ), classOf[Spectrum], classOf[AggregationResult]
      ).getMappedResults.asScala.headOption.getOrElse(AggregationResult(null, 0)).count


    // MetaData count
    val metaDataCount: Long = metaDataStatisticsService.countMetaDataStatistics


    // MetaData value count
    val metaDataValueCount: Long =
      mongoOperations.aggregate(
        newAggregation(
          classOf[Spectrum],
          project("metaData"),
          unwind("metaData"),
          group().count().as("count")
        ), classOf[Spectrum], classOf[AggregationResult]
      ).getMappedResults.asScala.headOption.getOrElse(AggregationResult(null, 0)).count


    // Tag count
    val tagCount: Long = tagStatisticsService.countTagStatistics


    // Tag value count
    val tagValueCount: Long =
      mongoOperations.aggregate(
        newAggregation(
          classOf[Spectrum],
          project("tags"),
          unwind("tags"),
          group().count().as("count")
        ), classOf[Spectrum], classOf[AggregationResult]
      ).getMappedResults.asScala.headOption.getOrElse(AggregationResult(null, 0)).count


    // Submitter count
    val submitterCount: Long =
      mongoOperations.aggregate(
        newAggregation(
          classOf[Spectrum],
          project("submitter"),
          group("submitter.emailAddress").count().as("count"),
          group().count().as("count")
        ), classOf[Spectrum], classOf[AggregationResult]
      ).getMappedResults.asScala.headOption.getOrElse(AggregationResult(null, 0)).count


    // Save global statistics
    globalStatisticsRepository.save(GlobalStatistics(null, new Date, spectrumCount, compoundCount, metaDataCount,
      metaDataValueCount, tagCount, tagValueCount, submitterCount))
  }

  /**
    *
    * @return
    */
  def getGlobalStatistics: GlobalStatistics =
    globalStatisticsRepository.findAll(new Sort(Sort.Direction.DESC, "date"))
      .asScala.headOption.getOrElse(GlobalStatistics(null, new Date, 0, 0, 0, 0, 0, 0, 0))


  /**
    * Update all statistics
    */
  @Async
  def updateStatistics() = {
    metaDataStatisticsService.updateMetaDataStatistics()
    tagStatisticsService.updateTagStatistics()
    updateGlobalStatistics()
  }
}


case class AggregationResult(_id: String, count: Long)