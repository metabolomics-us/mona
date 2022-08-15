package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.metadata

import java.util.concurrent.Future
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.service.MetaDataStatisticsService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.statistics.{MetaDataValueCount, StatisticsMetaData}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.{Async, AsyncResult}
import org.springframework.web.bind.annotation.{RequestParam, _}

import scala.jdk.CollectionConverters._

/**
  * Created by wohlg_000 on 3/7/2016.
  */
@CrossOrigin
@RestController
@RequestMapping(Array("/rest/metaData"))
class MetaDataRestController {

  @Autowired
  val metaDataStatisticsService: MetaDataStatisticsService = null


  /**
    * List unique metadata names and filter metadata names if query is given
    *
    * @return
    */
  @RequestMapping(path = Array("/names"), method = Array(RequestMethod.GET))
  @Async
  def listMetaDataName(@RequestParam(value = "search", required = false) partialMetaDataName: String): Future[Array[StatisticsMetaData.StatisticsMetaDataSummary]] = {
    if (partialMetaDataName == null || partialMetaDataName.isEmpty) {
      new AsyncResult[Array[StatisticsMetaData.StatisticsMetaDataSummary]](metaDataStatisticsService.getMetaDataNames)
    } else {
      new AsyncResult[Array[StatisticsMetaData.StatisticsMetaDataSummary]](
        metaDataStatisticsService
          .getMetaDataNames.filter(_.getName.toLowerCase.contains(partialMetaDataName.toLowerCase)))
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
                         @RequestParam(value = "search", required = false) partialMetaDataValue: String): AsyncResult[Iterable[MetaDataValueCount]] = {

    val metaDataStatistics: StatisticsMetaData = metaDataStatisticsService.getMetaDataStatistics(metaDataName)

    if (partialMetaDataValue == null || partialMetaDataValue.isEmpty) {
      new AsyncResult[Iterable[MetaDataValueCount]](metaDataStatistics.getMetaDataValueCount.asScala)
    } else {
      // Filter the metadata values and take the top 25
      new AsyncResult[Iterable[MetaDataValueCount]](
          metaDataStatistics.getMetaDataValueCount.asScala
            .filter(_.getValue.toLowerCase.contains(partialMetaDataValue.toLowerCase))
            .sortBy(-_.getCount)
            .take(25)
      )
    }
  }
}