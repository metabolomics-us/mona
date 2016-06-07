package edu.ucdavis.fiehnlab.mona.backend.services.downloader.controller

import java.util.concurrent.Future

import com.typesafe.scalalogging.LazyLogging
import io.swagger.annotations.ApiModel
import org.springframework.scheduling.annotation.{Async, AsyncResult}
import org.springframework.web.bind.annotation.{RequestMapping, RequestParam, RestController}

/**
  * Created by sajjan on 5/25/16.
  */
@RestController
@RequestMapping(value = Array("/rest/downloads"))
class DownloadSchedulerController extends LazyLogging {

  /**
    * schedules every spectra for the given query for curation
    *
    * @param query
    */
  @RequestMapping(path = Array("/schedule"))
  @Async
  def scheduleDownload(@RequestParam(required = true, name = "query") query: String): Future[DownloadJobScheduled] = {
    // Schedule download
//    val download = queryDownloadSchedulerService.scheduleDownload(query)

    new AsyncResult[DownloadJobScheduled](DownloadJobScheduled(query))
  }
}

@ApiModel
case class DownloadJobScheduled(id: String)

@ApiModel
case class DownloadJobError(error: String)