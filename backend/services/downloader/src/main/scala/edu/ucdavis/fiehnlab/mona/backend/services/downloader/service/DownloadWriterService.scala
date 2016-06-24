package edu.ucdavis.fiehnlab.mona.backend.services.downloader.service

import java.io.{BufferedWriter, FileInputStream, BufferedInputStream, FileOutputStream}
import java.lang.Iterable
import java.nio.charset.StandardCharsets
import java.nio.file.{Paths, Files, Path}
import java.util.zip.{ZipEntry, ZipOutputStream}

import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.MonaMapper
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.msp.MSPWriter
import edu.ucdavis.fiehnlab.mona.backend.core.domain.util.DynamicIterable
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.ISpectrumMongoRepositoryCustom
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.data.domain.{Page, Pageable}
import org.springframework.stereotype.Service

import scala.collection.JavaConverters._

/**
  * Created by sajjan on 6/10/16.
  */
@Service
class DownloadWriterService extends LazyLogging {

  @Autowired
  val mongoRepository: ISpectrumMongoRepositoryCustom = null

  @Value("${mona.export.path:#{systemProperties['java.io.tmpdir']}}#{systemProperties['file.separator']}mona_exports")
  val dir: String = null

  val objectMapper: ObjectMapper = MonaMapper.create


  /**
    *
    * @param query
    */
  def executeQuery(query: String): Iterable[Spectrum] = {
    if (query == null || query.isEmpty) {
      mongoRepository.findAll
    } else {
      new DynamicIterable[Spectrum, String](query, 10) {

        /**
          * Loads more data from the server for the given query
          */
        override def fetchMoreData(query: String, pageable: Pageable): Page[Spectrum] = {
          mongoRepository.rsqlQuery(query, pageable)
        }
      }
    }
  }

  /**
    *
    * @param bufferedWriter
    * @param query
    * @return
    */
  def downloadAsJSON(bufferedWriter: BufferedWriter, query: String): Int = {
    bufferedWriter.write("[")

    val count: Int = executeQuery(query).asScala.foldLeft(0) { (sum, spectrum: Spectrum) =>
      if (sum > 0) {
        bufferedWriter.write(",")
      }

      bufferedWriter.write(objectMapper.writeValueAsString(spectrum))
      sum + 1
    }

    bufferedWriter.write("]")

    count
  }

  /**
    *
    * @param bufferedWriter
    * @param query
    * @return
    */
  def downloadAsMSP(bufferedWriter: BufferedWriter, query: String): Int = {
    val mspWriter: MSPWriter = new MSPWriter

    val count: Int = executeQuery(query).asScala.foldLeft(0) { (sum, spectrum: Spectrum) =>
      if (sum > 0) {
        bufferedWriter.write("\n")
      }

      mspWriter.write(spectrum, bufferedWriter)
      sum + 1
    }

    count
  }

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


  def writeExportFile(exportFile: Path, compressedFile: Path, query: String, format: String, compress: Boolean): Int = {
    logger.info(s"Exporting spectra to file ${exportFile.getFileName}")

    val bufferedWriter = Files.newBufferedWriter(exportFile)

    val count: Int = format match {
      case "msp" => downloadAsMSP(bufferedWriter, query)
      case "json" | _ => downloadAsJSON(bufferedWriter, query)
    }

    bufferedWriter.close()

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
