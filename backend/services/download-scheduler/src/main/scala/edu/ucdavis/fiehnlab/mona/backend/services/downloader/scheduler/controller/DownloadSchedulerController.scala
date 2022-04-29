package edu.ucdavis.fiehnlab.mona.backend.services.downloader.scheduler.controller

import java.nio.file.{Files, Path, Paths}
import java.util.concurrent.Future
import javax.servlet.http.HttpServletRequest

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.core.repository.{PredefinedQueryMongoRepository, QueryExportMongoRepository}
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.core.types.{PredefinedQuery, QueryExport}
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.scheduler.service.DownloadSchedulerService
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.core.io.InputStreamResource
import org.springframework.http.{HttpStatus, MediaType, ResponseEntity}
import org.springframework.scheduling.annotation.{Async, AsyncResult}
import org.springframework.web.bind.annotation._

import scala.jdk.CollectionConverters._

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

  @Value("${mona.downloads:#{systemProperties['java.io.tmpdir']}}#{systemProperties['file.separator']}mona_downloads")
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

    logger.info(s"Starting download of $id...")

    val queryExport: QueryExport = queryExportRepository.findById(id).get()

    if (queryExport == null) {
      logger.info(s"\t-> Download object $id does not exist!")

      new AsyncResult[ResponseEntity[InputStreamResource]](new ResponseEntity(HttpStatus.NOT_FOUND))
    } else {
      val exportPath: Path = Paths.get(exportDir, queryExport.exportFile)

      logger.info(s"\t-> Attempting to download file ${exportPath.toAbsolutePath.toString}...")

      if (Files.exists(exportPath)) {
        new AsyncResult[ResponseEntity[InputStreamResource]](
          ResponseEntity.ok()
            .contentLength(Files.size(exportPath))
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header("Content-Disposition", s"attachment; filename=${exportPath.getFileName}")
            .body(new InputStreamResource(Files.newInputStream(exportPath)))
        )
      } else {
        logger.info(s"\t-> Download file ${exportPath.toAbsolutePath.toString} does not exist!")

        new AsyncResult[ResponseEntity[InputStreamResource]](new ResponseEntity(HttpStatus.NOT_FOUND))
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
                       @RequestParam(required = false, name = "format", defaultValue = "json") format: String): ResponseEntity[QueryExport] = {

    // Schedule download
    val downloadObject: QueryExport = downloadSchedulerService.scheduleExport(query, format)

    new ResponseEntity(downloadObject, HttpStatus.OK)
  }

  /**
    * Reschedule a query export with the given id
    *
    * @param id
    * @return
    */
  @RequestMapping(path = Array("/schedule/{id}"), method = Array(RequestMethod.GET))
  @ResponseBody
  def reschedule(@PathVariable("id") id: String): ResponseEntity[QueryExport] = {

    // Schedule export
    val downloadObject: QueryExport = downloadSchedulerService.scheduleExport(id)

    if (downloadObject == null) {
      new ResponseEntity(HttpStatus.NOT_FOUND)
    } else {
      new ResponseEntity(downloadObject, HttpStatus.OK)
    }
  }

  /**
    * Lists all available predefined exports
    */
  @RequestMapping(path = Array("/predefined"), method = Array(RequestMethod.GET))
  @Async
  def listPredefinedExports(): ResponseEntity[Array[PredefinedQuery]] = {
    new ResponseEntity(predefinedQueryRepository.findAll().asScala.toArray, HttpStatus.OK)
  }

  /**
    * Add a new predefined export
    *
    * @return
    */
  @RequestMapping(path = Array("/predefined"), method = Array(RequestMethod.POST))
  @Async
  def createPredefinedExport(@RequestBody query: PredefinedQuery): ResponseEntity[PredefinedQuery] = {
    new ResponseEntity(predefinedQueryRepository.save(query), HttpStatus.OK)
  }

  /**
    * Schedules the re-generation of predefined exports
    */
  @RequestMapping(path = Array("/generatePredefined"), method = Array(RequestMethod.GET))
  @Async
  def generatePredefinedExports(): ResponseEntity[Array[PredefinedQuery]] = {
    new ResponseEntity(downloadSchedulerService.generatePredefinedExports(), HttpStatus.OK)
  }

  /**
    * Schedules the re-generation of static exports
    */
  @RequestMapping(path = Array("/generateStatic"), method = Array(RequestMethod.GET))
  @Async
  def generateStaticExports(): ResponseEntity[Array[QueryExport]] = {
    new ResponseEntity(downloadSchedulerService.generateStaticExports(), HttpStatus.OK)
  }
}
