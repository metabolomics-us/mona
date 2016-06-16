package edu.ucdavis.fiehnlab.mona.backend.services.downloader.controller

import java.util.concurrent.Future

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.service.DownloadSchedulerService
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.types.QueryExport
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
  def scheduleDownload(@RequestParam(required = true, name = "query") query: String,
                       @RequestParam(required = false, name = "format", defaultValue = "json") format: String): Future[QueryExport] = {
    // Schedule download
    val downloadObject: QueryExport = downloadSchedulerService.scheduleDownload(query, format)

    new AsyncResult[QueryExport](downloadObject)
  }

  /**
    * schedules the re-generation of predefined downloads
    */
  @RequestMapping(path = Array("/schedulePredefinedDownloads"))
  @Async
  def schedulePredefinedDownloads(): Future[Array[QueryExport]] = {
    // Schedule download
    val downloadObjects: Array[QueryExport] = downloadSchedulerService.schedulePredefinedDownloads()

    new AsyncResult[Array[QueryExport]](downloadObjects)
  }
}