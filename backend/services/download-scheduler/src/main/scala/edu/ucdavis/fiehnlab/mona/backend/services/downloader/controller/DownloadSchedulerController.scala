package edu.ucdavis.fiehnlab.mona.backend.services.downloader.controller

import java.nio.file.{Files, Path, Paths}
import java.util.concurrent.Future
import javax.servlet.http.HttpServletRequest

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.repository.{PredefinedQueryMongoRepository, QueryExportMongoRepository}
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.service.DownloadSchedulerService
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.types.{PredefinedQuery, QueryExport}
import org.springframework.beans.factory.annotation.{Value, Autowired}
import org.springframework.core.io.InputStreamResource
import org.springframework.http.{MediaType, ResponseEntity}
import org.springframework.scheduling.annotation.{Async, AsyncResult}
import org.springframework.web.bind.annotation._
import org.springframework.web.servlet.mvc.multiaction.NoSuchRequestHandlingMethodException

import scala.collection.JavaConverters._

/**
  * Created by sajjan on 5/25/16.
  */
@RestController
@RequestMapping(value = Array("/rest/downloads"))
class DownloadSchedulerController extends LazyLogging {

  @Autowired
  val downloadSchedulerService: DownloadSchedulerService = null

  @Autowired
  val queryExportRepository: QueryExportMongoRepository = null

  @Autowired
  val predefinedQueryRepository: PredefinedQueryMongoRepository = null

  @Value("${mona.export.path:#{systemProperties['java.io.tmpdir']}}#{systemProperties['file.separator']}mona_exports")
  val exportDir: String = null

  /**
    * Downloads a query export given the label
    *
    * @param id
    * @param request
    * @return
    */
  @RequestMapping(path = Array("/retrieve/{id}"), method = Array(RequestMethod.GET))
  @Async
  def download(@PathVariable("id") id: String, request: HttpServletRequest): Future[ResponseEntity[InputStreamResource]] = {
    val queryExport: QueryExport = queryExportRepository.findOne(id)

    if (queryExport == null) {
      throw new NoSuchRequestHandlingMethodException(request)
    } else {
      val exportPath: Path = Paths.get(exportDir, queryExport.exportFile)

      if (Files.exists(exportPath)) {
        new AsyncResult[ResponseEntity[InputStreamResource]](
          ResponseEntity
            .ok()
            .contentLength(Files.size(exportPath))
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header("Content-Disposition", s"attachment; filename=${exportPath.getFileName}")
            .body(new InputStreamResource(Files.newInputStream(exportPath)))
        )
      } else {
        throw new NoSuchRequestHandlingMethodException(request)
      }
    }
  }

  /**
    * schedules every spectra for the given query for curation
    *
    * @param query
    */
  @RequestMapping(path = Array("/schedule"), method = Array(RequestMethod.GET))
  @Async
  def scheduleDownload(@RequestParam(required = true, name = "query") query: String,
                       @RequestParam(required = false, name = "format", defaultValue = "json") format: String): Future[QueryExport] = {
    // Schedule download
    val downloadObject: QueryExport = downloadSchedulerService.scheduleDownload(query, format)

    new AsyncResult[QueryExport](downloadObject)
  }

  /**
    * lists all available predefined downloads
    */
  @RequestMapping(path = Array("/predefined"), method = Array(RequestMethod.GET))
  @Async
  def listPredefinedDownloads(): Future[Array[PredefinedQuery]] = {
    val data = predefinedQueryRepository.findAll().asScala.toArray

    new AsyncResult[Array[PredefinedQuery]](data)
  }

  /**
    * schedules the re-generation of predefined downloads
    */
  @RequestMapping(path = Array("/schedulePredefined"), method = Array(RequestMethod.GET))
  @Async
  def schedulePredefinedDownloads(): Future[Array[QueryExport]] = {
    // Schedule download
    val downloadObjects: Array[QueryExport] = downloadSchedulerService.schedulePredefinedDownloads()

    new AsyncResult[Array[QueryExport]](downloadObjects)
  }
}