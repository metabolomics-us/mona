package edu.ucdavis.fiehnlab.mona.backend.services.downloader.service

import java.io.{BufferedInputStream, FileInputStream, FileOutputStream}
import java.lang.Iterable
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}
import java.util.zip.{ZipEntry, ZipOutputStream}

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.writer.{JSONDownloader, MSPDownloader}
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.stereotype.Service

import scala.collection.JavaConverters._

/**
  * Created by sajjan on 6/10/16.
  */
@Service
class DownloadWriterService extends LazyLogging {

  @Autowired
  val mspDownloader: MSPDownloader = null

  @Autowired
  val jsonDownloader: JSONDownloader = null


  /**
    *
    * @param queryFile
    * @param query
    * @return
    */
  def writeQueryFile(queryFile: Path, query: String) = {
    logger.info(s"Exporting query file ${queryFile.getFileName}")

    Files.write(queryFile, query.getBytes(StandardCharsets.UTF_8))
  }


  def writeExportFile(exportFile: Path, compressedFile: Path, query: String, format: String, compress: Boolean): Long = {
    logger.info(s"Exporting spectra to file ${exportFile.getFileName}")

    val count: Long = format match {
      case "msp" => mspDownloader.write(query, exportFile)
      case "json" | _ => jsonDownloader.write(query, exportFile)
    }


    logger.info(s"Finished exporting $count spectra")


    // Compress results
    if (compress) {
      logger.info(s"Compressing ${exportFile.getFileName} -> ${compressedFile.getFileName}")

      val compressedTemporaryFile: Path = Paths.get(compressedFile.getParent.toAbsolutePath.toString, compressedFile.getFileName.toString +".tmp")

      val zipFile: ZipOutputStream = new ZipOutputStream(new FileOutputStream(compressedTemporaryFile.toAbsolutePath.toString))

      zipFile.putNextEntry(new ZipEntry(exportFile.getFileName.toString))

      val inputStream: BufferedInputStream = new BufferedInputStream(new FileInputStream(exportFile.toAbsolutePath.toString))
      val buffer: Array[Byte] = new Array[Byte](1024)
      var length: Int = inputStream.read(buffer, 0, 1024)


      while (length != -1) {
        zipFile.write(buffer, 0, length)
        length = inputStream.read(buffer, 0, 1024)
      }

      inputStream.close()
      zipFile.closeEntry()
      zipFile.close()

      // Delete exported file and move temporary export file to stored location
      Files.deleteIfExists(exportFile)
      Files.deleteIfExists(compressedFile)
      Files.move(compressedTemporaryFile, compressedFile)
    }

    count
  }
}
