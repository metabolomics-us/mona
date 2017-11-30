package edu.ucdavis.fiehnlab.mona.backend.core.statistics.service

import java.util.Date

import com.mongodb.{BasicDBObject, DBObject}
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.ISpectrumMongoRepositoryCustom
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.repository.GlobalStatisticsMongoRepository
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.types.GlobalStatistics
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.aggregation.Aggregation._
import org.springframework.data.mongodb.core.aggregation.{AggregationOperation, AggregationOperationContext}
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.scheduling.annotation.{Async, Scheduled}
import org.springframework.stereotype.Service

import scala.collection.JavaConverters._

/**
  * Created by sajjan on 8/2/16.
  */
@Service
class StatisticsService extends LazyLogging {

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

  @Autowired
  val submitterStatisticsService: SubmitterStatisticsService = null

  @Autowired
  val compoundClassStatisticsService: CompoundClassStatisticsService = null


  /**
    * Update the data in the global statistics repository
    *
    * @return
    */
  def updateGlobalStatistics(): GlobalStatistics = {
    // Spectrum count
    val spectrumCount: Long = spectrumMongoRepository.count()


    // Compound count
    val compoundCount: Long =
      mongoOperations.aggregate(
        newAggregation(
          classOf[Spectrum],
          project("compound"),
          unwind("compound"),
          unwind("compound.metaData"),
          `match`(Criteria.where("compound.metaData.name").is("InChIKey")),
          project().and("compound.metaData.value").as("value"),

          // Get first InChIKey block and group
          new AggregationOperation() {
            override def toDBObject(context: AggregationOperationContext): DBObject =
              context.getMappedObject(new BasicDBObject(
                "$project", new BasicDBObject(
                  "value", new BasicDBObject(
                    "$substr", Array("$value", 0, 14)
                  )
                )
              ))
          },

          group("value"),
          group().count().as("count")
        ).withOptions(newAggregationOptions().allowDiskUse(true).build()),
        classOf[Spectrum], classOf[AggregationResult]
      ).getMappedResults.asScala.headOption.getOrElse(AggregationResult(null, 0)).count


    // MetaData count
    val metaDataCount: Long =
      mongoOperations.aggregate(
        newAggregation(
          classOf[Spectrum],
          project("metaData"),
          unwind("metaData"),
          group("metaData.name"),
          group().count().as("count")
        ).withOptions(newAggregationOptions().allowDiskUse(true).build()),
        classOf[Spectrum], classOf[AggregationResult]
      ).getMappedResults.asScala.headOption.getOrElse(AggregationResult(null, 0)).count


    // MetaData value count
    val metaDataValueCount: Long =
      mongoOperations.aggregate(
        newAggregation(
          classOf[Spectrum],
          project("metaData"),
          unwind("metaData"),
          group().count().as("count")
        ).withOptions(newAggregationOptions().allowDiskUse(true).build()),
        classOf[Spectrum], classOf[AggregationResult]
      ).getMappedResults.asScala.headOption.getOrElse(AggregationResult(null, 0)).count


    // Tag count
    val tagCount: Long =
      mongoOperations.aggregate(
        newAggregation(
          classOf[Spectrum],
          project("tags"),
          unwind("tags"),
          group("tags.text"),
          group().count().as("count")
        ).withOptions(newAggregationOptions().allowDiskUse(true).build()),
        classOf[Spectrum], classOf[AggregationResult]
      ).getMappedResults.asScala.headOption.getOrElse(AggregationResult(null, 0)).count


    // Tag value count
    val tagValueCount: Long =
      mongoOperations.aggregate(
        newAggregation(
          classOf[Spectrum],
          project("tags"),
          unwind("tags"),
          group().count().as("count")
        ).withOptions(newAggregationOptions().allowDiskUse(true).build()),
        classOf[Spectrum], classOf[AggregationResult]
      ).getMappedResults.asScala.headOption.getOrElse(AggregationResult(null, 0)).count


    // Submitter count
    val submitterCount: Long =
      mongoOperations.aggregate(
        newAggregation(
          classOf[Spectrum],
          project("submitter"),
          group("submitter.emailAddress").count().as("count"),
          group().count().as("count")
        ).withOptions(newAggregationOptions().allowDiskUse(true).build()),
        classOf[Spectrum], classOf[AggregationResult]
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
  @Scheduled(cron = "0 0 0 * * *")
  def updateStatistics(): Unit = {
    metaDataStatisticsService.updateMetaDataStatistics()
    tagStatisticsService.updateTagStatistics()
    submitterStatisticsService.updateSubmitterStatistics()
    compoundClassStatisticsService.updateCompoundClassStatistics()
    updateGlobalStatistics()
  }
}


case class AggregationResult(_id: String, count: Long)