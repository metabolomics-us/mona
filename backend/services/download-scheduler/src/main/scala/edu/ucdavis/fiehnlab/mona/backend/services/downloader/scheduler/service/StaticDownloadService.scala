package edu.ucdavis.fiehnlab.mona.backend.services.downloader.scheduler.service

import java.io.IOException
import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.domain.StaticDownload
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

import scala.jdk.CollectionConverters._


@Service
class StaticDownloadService extends LazyLogging {

  @Value("${mona.downloads:#{systemProperties['java.io.tmpdir']}}#{systemProperties['file.separator']}mona_downloads")
  val exportDir: String = null

  def staticDownloadDir: Path = Paths.get(exportDir, "static")

  /**
    * Retrieves a file path relative to the static downloads folder
    *
    * @param filePath
    * @return
    */
  def getFilePath(filePath: String): Path = {
    staticDownloadDir.resolve(filePath)
  }

  /**
    * Purges the static download directory if it exists
    */
  def removeStaticDownloadDirectory(): Unit = {
    if (Files.exists(staticDownloadDir)) {
      Files.walkFileTree(staticDownloadDir, new SimpleFileVisitor[Path] {
        override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
          Files.delete(file)
          FileVisitResult.CONTINUE
        }

        override def postVisitDirectory(dir: Path, exc: IOException): FileVisitResult = {
          Files.delete(dir)
          FileVisitResult.CONTINUE
        }
      })
    }
  }

  /**
    * Recursively generates a list of files within the static download directory
    *
    * @return
    */
  def listStaticDownloads(): Array[StaticDownload] = {
    if (Files.notExists(staticDownloadDir)) {
      Files.createDirectories(staticDownloadDir)
    }

    Files.walk(staticDownloadDir)
      .iterator()
      .asScala
      .filter(Files.isRegularFile(_))
      .filter(!_.toString.endsWith(".description.txt"))
      .filter(!_.toString.endsWith(".tmp"))
      .map { file =>
        // Read description if it exists
        val descriptionFile: Path = Paths.get(file.toAbsolutePath +".description.txt")

        if (Files.exists(descriptionFile)) {
          new StaticDownload(staticDownloadDir.relativize(file).toString, new String(Files.readAllBytes(descriptionFile)))
        } else {
          new StaticDownload(staticDownloadDir.relativize(file).toString)
        }
      }.toArray
  }

  /**
    * Store a file in the specified category
    *
    * @param file
    * @param category
    * @return
    */
  def storeStaticFile(file: MultipartFile, category: String = null, description: String = null): String = {
    // Build file path
    val exportPath: Path = category match {
      case null => staticDownloadDir
      case _: String => getFilePath(category)
    }

    // Create export/category directory if it doesn't exist
    if (Files.notExists(exportPath)) {
      Files.createDirectories(exportPath)
    }

    // Specify export file
    val exportFilename: String =
      if (file.getOriginalFilename.nonEmpty) file.getOriginalFilename
      else file.getName
    val exportFile: Path = exportPath.resolve(exportFilename)

    logger.info(s"Storing file $exportFile")

    // Write the MultipartFile and to the static download directory
    Files.copy(file.getInputStream, exportFile, StandardCopyOption.REPLACE_EXISTING)

    // Export description if available
    if (description != null) {
      val descrptionFilename: String = exportFilename +".description.txt"

      logger.info(s"Storing description file $descrptionFilename")
      Files.write(exportPath.resolve(descrptionFilename), description.getBytes)
    }

    // Return the relative file path
    staticDownloadDir.relativize(exportFile).toString
  }

  /**
    * Checks if a path (relative to the static downloads folder) exists as a file
    *
    * @param filePath
    * @return
    */
  def fileExists(filePath: String): Boolean = {
    val file: Path = getFilePath(filePath)

    Files.exists(file) && Files.isRegularFile(file)
  }
}
