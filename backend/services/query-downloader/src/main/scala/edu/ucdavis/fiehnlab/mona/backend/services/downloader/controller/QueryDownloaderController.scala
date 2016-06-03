package edu.ucdavis.fiehnlab.mona.backend.services.downloader.controller

import java.util.concurrent.Future

import edu.ucdavis.fiehnlab.mona.backend.services.downloader.repository.IQueryExportMongoRepository
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.service.QueryDownloadSchedulerService
import io.swagger.annotations.ApiModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.{AsyncResult, Async}
import org.springframework.web.bind.annotation.{CrossOrigin, RequestParam, RequestMapping, RestController}

/**
  * Created by sajjan on 6/1/16.
  */
@RestController
@RequestMapping(value = Array("/rest/downloads"))
class QueryDownloaderController {

  @Autowired
  val queryDownloadSchedulerService: QueryDownloadSchedulerService = null

  /**
    * schedules every spectra for the given query for curation
    *
    * @param query
    */
  @RequestMapping(path = Array("/schedule"))
  @Async
  def scheduleDownload(@RequestParam(required = true, name = "query") query: String): Future[QueryDownloadJobScheduled] = {
    // Schedule download
    val download = queryDownloadSchedulerService.scheduleDownload(query)

    new AsyncResult[QueryDownloadJobScheduled](QueryDownloadJobScheduled(101))
  }
}

@ApiModel
case class QueryDownloadJobScheduled(count: Int)

@ApiModel
case class QueryDownloadJobError(count: Int)