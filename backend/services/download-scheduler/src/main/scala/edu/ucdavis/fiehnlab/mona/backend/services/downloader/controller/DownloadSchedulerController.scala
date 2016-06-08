package edu.ucdavis.fiehnlab.mona.backend.services.downloader.controller

import java.util.UUID
import java.util.concurrent.Future

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.service.{ScheduledDownload, DownloadSchedulerService}
import io.swagger.annotations.ApiModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.{Async, AsyncResult}
import org.springframework.web.bind.annotation.{RequestMapping, RequestParam, RestController}

/**
  * Created by sajjan on 5/25/16.
  */
@RestController
@RequestMapping(value = Array("/rest/downloads"))
class DownloadSchedulerController extends LazyLogging {

  @Autowired
  val downloadSchedulerService: DownloadSchedulerService = null

  /**
    * schedules every spectra for the given query for curation
    *
    * @param query
    */
  @RequestMapping(path = Array("/schedule"))
  @Async
  def scheduleDownload(@RequestParam(required = true, name = "query") query: String): Future[ScheduledDownload] = {
    // Schedule download
    val downloadObject: ScheduledDownload = downloadSchedulerService.scheduleDownload(query)

    new AsyncResult[ScheduledDownload](downloadObject)
  }

  /**
    * schedules the re-generation of predefined downloads
    */
  @RequestMapping(path = Array("/schedulePredefinedDownloads"))
  @Async
  def schedulePredefinedDownloads(): Future[ScheduledDownload] = {
    // Schedule download
    val downloadObject: ScheduledDownload = downloadSchedulerService.schedulePredefinedDownloads

    new AsyncResult[ScheduledDownload](downloadObject)
  }
}