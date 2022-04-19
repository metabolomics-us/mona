package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.metadata

import java.util.concurrent.Future
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.service.{CompoundMetaDataStatisticsService, MetaDataStatisticsService}
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.types.{CompoundMetaDataStatistics, CompoundMetaDataStatisticsSummary, MetaDataStatistics, MetaDataStatisticsSummary}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.scheduling.annotation.{Async, AsyncResult}
import org.springframework.web.bind.annotation.{RequestParam, _}

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
  val metaDataStatisticsService: MetaDataStatisticsService = null

  @Autowired
  val compoundMetaDataStatisticsService: CompoundMetaDataStatisticsService = null


  /**
    * List unique metadata names and filter metadata names if query is given
    *
    * @return
    */
  @RequestMapping(path = Array("/names"), method = Array(RequestMethod.GET))
  @Async
  def listMetaDataName(@RequestParam(value = "search", required = false) partialMetaDataName: String): Future[Array[MetaDataStatisticsSummary]] = {
    if (partialMetaDataName == null || partialMetaDataName.isEmpty) {
      new AsyncResult[Array[MetaDataStatisticsSummary]](metaDataStatisticsService.getMetaDataNames)
    } else {
      new AsyncResult[Array[MetaDataStatisticsSummary]](
        metaDataStatisticsService
          .getMetaDataNames.filter(_.name.toLowerCase.contains(partialMetaDataName.toLowerCase)))
    }
  }

  /**
    * List unique metadata values for a given metadata name and search values if query is given
    *
    * @param metaDataName
    * @return
    */
  @RequestMapping(path = Array("/values"), method = Array(RequestMethod.GET))
  @Async
  def listMetaDataValues(@RequestParam(value = "name", required = true) metaDataName: String,
                         @RequestParam(value = "search", required = false) partialMetaDataValue: String): AsyncResult[MetaDataStatistics] = {

    val metaDataStatistics: MetaDataStatistics = metaDataStatisticsService.getMetaDataStatistics(metaDataName)

    if (partialMetaDataValue == null || partialMetaDataValue.isEmpty) {
      new AsyncResult[MetaDataStatistics](metaDataStatistics)
    } else {
      // Filter the metadata values and take the top 25
      new AsyncResult[MetaDataStatistics](
        metaDataStatistics.copy(values =
          metaDataStatistics.values
            .filter(_.value.toLowerCase.contains(partialMetaDataValue.toLowerCase))
            .sortBy(-_.count)
            .take(25)
        )
      )
    }
  }

  /**
   * List unique metadata names and filter metadata names if query is given
   *
   * @return
   */
  @RequestMapping(path = Array("/compound/names"), method = Array(RequestMethod.GET))
  @Async
  def listCompoundMetaDataName(@RequestParam(value = "search", required = false) partialMetaDataName: String): Future[Array[CompoundMetaDataStatisticsSummary]] = {
    if (partialMetaDataName == null || partialMetaDataName.isEmpty) {
      new AsyncResult[Array[CompoundMetaDataStatisticsSummary]](compoundMetaDataStatisticsService.getCompoundMetaDataNames)
    } else {
      new AsyncResult[Array[CompoundMetaDataStatisticsSummary]](
        compoundMetaDataStatisticsService
          .getCompoundMetaDataNames.filter(_.name.toLowerCase.contains(partialMetaDataName.toLowerCase)))
    }
  }

  /**
   * List unique metadata values for a given metadata name and search values if query is given
   *
   * @param metaDataName
   * @return
   */
  @RequestMapping(path = Array("/compound/values"), method = Array(RequestMethod.GET))
  @Async
  def listCompoundMetaDataValues(@RequestParam(value = "name", required = true) metaDataName: String,
                         @RequestParam(value = "search", required = false) partialMetaDataValue: String): AsyncResult[CompoundMetaDataStatistics] = {

    val compoundMetaDataStatistics: CompoundMetaDataStatistics = compoundMetaDataStatisticsService.getCompoundMetaDataStatistics(metaDataName)

    if (partialMetaDataValue == null || partialMetaDataValue.isEmpty) {
      new AsyncResult[CompoundMetaDataStatistics](compoundMetaDataStatistics)
    } else {
      // Filter the metadata values and take the top 25
      new AsyncResult[CompoundMetaDataStatistics](
        compoundMetaDataStatistics.copy(values =
          compoundMetaDataStatistics.values
            .filter(_.value.toLowerCase.contains(partialMetaDataValue.toLowerCase))
            .sortBy(-_.count)
            .take(25)
        )
      )
    }
  }
}
