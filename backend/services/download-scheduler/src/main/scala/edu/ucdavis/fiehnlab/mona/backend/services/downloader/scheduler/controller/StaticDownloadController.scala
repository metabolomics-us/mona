package edu.ucdavis.fiehnlab.mona.backend.services.downloader.scheduler.controller

import java.nio.file.{Files, Path}

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.scheduler.service.StaticDownloadService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.InputStreamResource
import org.springframework.http.{HttpStatus, MediaType, ResponseEntity}
import org.springframework.web.bind.annotation._
import org.springframework.web.multipart.MultipartFile

/**
  * Created by sajjan on 5/25/16.
  */
@RestController
@RequestMapping(value = Array("/rest/downloads"))
class StaticDownloadController extends LazyLogging {

  @Autowired
  val staticDownloadService: StaticDownloadService = null


  /**
    * List all available static downloads
    */
  @RequestMapping(path = Array("/static"), method = Array(RequestMethod.GET))
  def listStaticDownloads(): Array[StaticDownload] = {
    staticDownloadService.listStaticDownloads().map(StaticDownload(_))
  }

  @RequestMapping(path = Array("/static"), method = Array(RequestMethod.POST))
  def uploadStaticDownload(@RequestParam(value = "file", required = true) file: MultipartFile, @RequestParam(value = "category", required = false) category: String): StaticDownload = {
    StaticDownload(staticDownloadService.storeStaticFile(file, category))
  }

  @RequestMapping(path = Array("/static/{filename:.+}"), method = Array(RequestMethod.GET))
  def getStaticDownload(@PathVariable("filename") filename: String): ResponseEntity[InputStreamResource] = {
    logger.info(filename)

    if (staticDownloadService.fileExists(filename)) {
      val filePath: Path = staticDownloadService.getFilePath(filename)

      ResponseEntity.ok()
        .contentLength(Files.size(filePath))
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .header("Content-Disposition", s"attachment; filename=$filename")
        .body(new InputStreamResource(Files.newInputStream(filePath)))
    } else {
      new ResponseEntity[InputStreamResource](HttpStatus.NOT_FOUND)
    }
  }

  @RequestMapping(path = Array("/static/{category:.+}/{filename:.+}"), method = Array(RequestMethod.GET))
  def getStaticDownload(@PathVariable("category") category: String, @PathVariable("filename") filename: String): ResponseEntity[InputStreamResource] = {
    logger.info(s"$category$filename")

    getStaticDownload(s"$category/$filename")
  }
}


case class StaticDownload(fileName: String, category: String)

object StaticDownload {
  def apply(filePath: String): StaticDownload = {
    val path: Array[String] = filePath.split('/')

    if (path.length == 1) {
      StaticDownload(filePath, null)
    } else {
      StaticDownload(path.last, path.head)
    }
  }
}