package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.statistics

import java.util.concurrent.Future

import edu.ucdavis.fiehnlab.mona.backend.core.statistics.service._
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.types._
import org.springframework.beans.factory.annotation.Autowired
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

  @Autowired
  val compoundClassStatisticsService: CompoundClassStatisticsService = null

  @Autowired
  val metaDataStatisticsService: MetaDataStatisticsService = null

  @Autowired
  val tagStatisticsService: TagStatisticsService = null

  @Autowired
  val submitterStatisticsService: SubmitterStatisticsService = null


  /**
    * Get a list of unique tags and their respective counts
    * @return
    */
  @RequestMapping(path = Array("/tags"), method = Array(RequestMethod.GET))
  @Async
  def listTags: Future[Iterable[TagStatistics]] = new AsyncResult[Iterable[TagStatistics]](tagStatisticsService.getTagStatistics)

  /**
    * Get a list of unique library tags and their respective counts
    * @return
    */
  @RequestMapping(path = Array("/tags/library"), method = Array(RequestMethod.GET))
  @Async
  def listLibraryTags: Future[Iterable[TagStatistics]] = new AsyncResult[Iterable[TagStatistics]](tagStatisticsService.getLibraryTagStatistics)

  /**
    * Get all metadata statistics
    * @return
    */
  @RequestMapping(path = Array("/statistics/metaData"), method = Array(RequestMethod.GET))
  @Async
  def listMetaData: Future[Iterable[MetaDataStatistics]] = new AsyncResult[Iterable[MetaDataStatistics]](metaDataStatisticsService.getMetaDataStatistics)

  /**
    * Get all metadata statistics
    * @return
    */
  @RequestMapping(path = Array("/statistics/global"), method = Array(RequestMethod.GET))
  @Async
  def getGlobalStatistics: Future[GlobalStatistics] = new AsyncResult[GlobalStatistics](statisticsService.getGlobalStatistics)

  /**
    * Get all compound class statistics
    * @return
    */
  @RequestMapping(path = Array("/statistics/compoundClasses"), method = Array(RequestMethod.GET))
  @Async
  def getCompoundClassStatistics: Future[Iterable[CompoundClassStatistics]] =
    new AsyncResult[Iterable[CompoundClassStatistics]](compoundClassStatisticsService.getCompoundClassStatistics)

  /**
    * Get all submitter statistics
    * @return
    */
  @RequestMapping(path = Array("/statistics/submitters"), method = Array(RequestMethod.GET))
  @Async
  def getSubmitterStatistics: Future[Iterable[SubmitterStatistics]] =
    new AsyncResult[Iterable[SubmitterStatistics]](submitterStatisticsService.getSubmitterStatistics)



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