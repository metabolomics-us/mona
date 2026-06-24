package edu.ucdavis.fiehnlab.mona.backend.services.statistics.server.controller.statistics

import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicBoolean
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.service._
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.scheduling.annotation.AsyncResult
import org.springframework.web.bind.annotation._
import edu.ucdavis.fiehnlab.mona.backend.services.statistics.server.service.StatisticsUpdateRunner
import edu.ucdavis.fiehnlab.mona.backend.core.domain.statistics.StatisticsTag
import edu.ucdavis.fiehnlab.mona.backend.core.domain.statistics.StatisticsMetaData
import edu.ucdavis.fiehnlab.mona.backend.core.domain.statistics.StatisticsGlobal
import edu.ucdavis.fiehnlab.mona.backend.core.domain.statistics.StatisticsCompoundClasses
import edu.ucdavis.fiehnlab.mona.backend.core.domain.statistics.StatisticsSubmitter
import org.springframework.context.annotation.Profile


/**
  * Created by sajjan on 8/4/16.
**/
@CrossOrigin
@RestController
@RequestMapping(Array("/rest"))
@Profile(Array("mona.persistence"))
class StatisticsRestController extends LazyLogging {

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

  @Autowired
  val statisticsUpdateRunner: StatisticsUpdateRunner = null

  // Guards against a second update being scheduled while one is still running
  private val updateInProgress: AtomicBoolean = new AtomicBoolean(false)

  /**
    * Get a list of unique tags and their respective counts
    *
    * @return
   * */
  @RequestMapping(path = Array("/tags"), method = Array(RequestMethod.GET))
  def listTags: Future[Iterable[StatisticsTag]] = new AsyncResult[Iterable[StatisticsTag]](tagStatisticsService.getTagStatistics)

  /**
    * Get a list of unique library tags and their respective counts
    *
    * @return
   * */
  @RequestMapping(path = Array("/tags/library"), method = Array(RequestMethod.GET))
  def listLibraryTags: Future[Iterable[StatisticsTag]] = new AsyncResult[Iterable[StatisticsTag]](tagStatisticsService.getLibraryTagStatistics)

  /**
    * Recompute the tag statistics from live data and return the refreshed library tags. Lets the
    * admin library list reflect deletions immediately instead of waiting for the nightly statistics run
    *
    * @return
   * */
  @RequestMapping(path = Array("/tags/library/refresh"), method = Array(RequestMethod.POST))
  @ResponseBody
  def refreshLibraryTags: Iterable[StatisticsTag] = {
    logger.info("Refreshing libraries now...")
    tagStatisticsService.updateTagStatistics()
    tagStatisticsService.getLibraryTagStatistics
  }

  /**
    * Get all metadata statistics
    *
    * @return
   * */
  @RequestMapping(path = Array("/statistics/metaData"), method = Array(RequestMethod.GET))
  def listMetaData: Future[Iterable[StatisticsMetaData]] = new AsyncResult[Iterable[StatisticsMetaData]](metaDataStatisticsService.getMetaDataStatistics)

  /**
    * Get all metadata statistics
    *
    * @return
   * */
  @RequestMapping(path = Array("/statistics/global"), method = Array(RequestMethod.GET))
  def getGlobalStatistics: Future[StatisticsGlobal] = new AsyncResult[StatisticsGlobal](statisticsService.getGlobalStatistics)

 /**
    * Get all compound class statistics
    *
    * @return
    * */
  @RequestMapping(path = Array("/statistics/compoundClasses"), method = Array(RequestMethod.GET))
  def getCompoundClassStatistics: Future[Iterable[StatisticsCompoundClasses]] =
    new AsyncResult[Iterable[StatisticsCompoundClasses]](compoundClassStatisticsService.getCompoundClassStatistics)

  /**
    * Get all submitter statistics
    *
    * @return
   * */
  @RequestMapping(path = Array("/statistics/submitters"), method = Array(RequestMethod.GET))
  def getSubmitterStatistics: Future[Iterable[StatisticsSubmitter]] =
    new AsyncResult[Iterable[StatisticsSubmitter]](submitterStatisticsService.getSubmitterStatistics)


  /**
    * Update statistics
    *
    * @return
   * */
  @RequestMapping(path = Array("/statistics/update"), method = Array(RequestMethod.POST))
  def updateStatistics(): ResponseEntity[String] = {
    // Reject if an update is already running so we never schedule a second concurrent recompute
    if (!updateInProgress.compareAndSet(false, true)) {
      new ResponseEntity[String]("Statistics update already in progress", HttpStatus.CONFLICT)
    } else {
      // Delegated to an @Async runner bean so the request returns immediately
      // The response confirms the update was requested, not that the recompute has finished
      // The runner clears updateInProgress in a finally block when the recompute completes
      statisticsUpdateRunner.runUpdate(updateInProgress)
      new ResponseEntity[String]("Statistics update requested", HttpStatus.ACCEPTED)
    }
  }
}
