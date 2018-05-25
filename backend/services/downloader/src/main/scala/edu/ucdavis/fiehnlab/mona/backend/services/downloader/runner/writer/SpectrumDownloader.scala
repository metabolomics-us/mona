package edu.ucdavis.fiehnlab.mona.backend.services.downloader.runner.writer

import java.io.{BufferedInputStream, BufferedWriter, FileInputStream}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}
import java.util.zip.{ZipEntry, ZipOutputStream}
import java.util.{Date, UUID}

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.core.types.{PredefinedQuery, QueryExport}

/**
  * Created by sajjan on 9/13/16.
  */
abstract class SpectrumDownloader(export: QueryExport, downloadDir: Path, compress: Boolean = true) extends LazyLogging {

  /**
    * Basename for this export, consisting of the sanitized label if it exists and otherwise the unique ID
    */
  protected val basename: String = buildExportBasename()

  /**
    * Filename for this export
    *
    * @return
    */
  protected def exportFilename: String = s"MoNA-export-$basename.${export.format}"

  /**
    * Filename for compressed export
    *
    * @return
    */
  protected def compressedExportFilename: String = {
    if (exportFilename.contains('.')) {
      s"${exportFilename.substring(0, exportFilename.lastIndexOf('.'))}-${export.format}.zip"
    } else {
      s"$exportFilename-${export.format}.zip"
    }
  }

  /**
    * Filename for query export
    */
  protected def queryFilename: String = s"MoNA-export-$basename-query.txt"

  /**
    * Defines the path to the export file
    *
    * @return
    */
  protected def exportFile: Path = downloadDir.resolve(exportFilename)

  /**
    * Spectrum counter
    */
  private var counter: Int = 0

  /**
    * Content prefix for this file format
    *
    * @return
    */
  protected def getContentPrefix: String

  /**
    * Content suffix for this file format
    *
    * @return
    */
  protected def getContentSuffix: String

  /**
    * Record separator for this file format
    *
    * @return
    */
  protected def getRecordSeparator: String


  /**
    *
    * @return
    */
  private def getLabel: String = {
    if (export.label == null || export.label.isEmpty)
      export.id
    else
      export.label
  }

  /**
    *
    * @return
    */
  private def buildExportBasename(): String = {
    val sanitizedLabel: String = getLabel.split(" - ").last.replaceAll(" ", "_").replaceAll("/", "-")

    if (export.emailAddress == null || export.emailAddress.isEmpty) {
      sanitizedLabel
    } else {
      export.emailAddress.split("@").head + '-' + sanitizedLabel
    }
  }


  /**
    * Export file writer definition
    */
  protected lazy val exportWriter: BufferedWriter = Files.newBufferedWriter(exportFile)

  /**
    * Initialize buffered writer and write the file prefix
    */
  def initializeExport(): Unit = {
    logger.info(s"Exporting query file ${exportFile.getFileName}")
    exportWriter.write(getContentPrefix)
  }

  /**
    * Handle a spectrum object
    */
  def write(spectrum: Spectrum): Unit = {
    if (counter > 0)
      exportWriter.write(getRecordSeparator)

    writeSpectrum(spectrum)

    counter += 1
  }

  /**
    * Write a spectrum to the export file in the specified format
    *
    * @param spectrum
    */
  protected def writeSpectrum(spectrum: Spectrum): Unit

  /**
    * close the export and compress if requested
    */
  def closeExport(): Unit = {
    exportWriter.write(getContentSuffix)
    exportWriter.close()

    if (compress) {
      logger.info(s"Compressing ${exportFile.getFileName} -> $compressedExportFilename")

      val compressedFile: Path = downloadDir.resolve(compressedExportFilename)
      val compressedTemporaryFile: Path = downloadDir.resolve(compressedExportFilename + ".tmp")

      // Setup zip export
      val zipFile: ZipOutputStream = new ZipOutputStream(Files.newOutputStream(compressedTemporaryFile))
      zipFile.putNextEntry(new ZipEntry(exportFilename))

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

    // Export additional associated files
    writeAssociatedFiles()
  }

  /**
    *
    * @return
    */
  protected def writeQueryFile(): Unit = {
    val queryFile: Path = downloadDir.resolve(queryFilename)

    logger.info(s"Exporting query file ${queryFile.getFileName}")
    Files.write(queryFile, export.query.getBytes(StandardCharsets.UTF_8))
  }

  /**
    *
    */
  protected def writeAssociatedFiles(): Unit = {
    writeQueryFile()
  }

  /**
    *
    * @return
    */
  def toQueryExport: QueryExport = {
    // Update export object with filesize, query count and filenames
    if (compress) {
      export.copy(
        label = getLabel,
        count = counter,
        date = new Date,
        size = Files.size(downloadDir.resolve(compressedExportFilename)),

        queryFile = queryFilename,
        exportFile = compressedExportFilename
      )
    } else {
      export.copy(
        label = getLabel,
        count = counter,
        date = new Date,
        size = Files.size(exportFile),

        queryFile = queryFilename,
        exportFile = exportFilename
      )
    }
  }
}

object SpectrumDownloader {

  /**
    * Create new Downloader from a predefined query
    *
    * @param query
    * @param export
    * @param format
    * @param downloadDir
    * @param compress
    * @return
    */
  def apply(query: PredefinedQuery, export: QueryExport, format: String, downloadDir: Path,
                    compress: Boolean): SpectrumDownloader = export match {

    case null => SpectrumDownloader(query.label, query.query, format, downloadDir, compress)
    case _: QueryExport => SpectrumDownloader(export, format, downloadDir, compress)
  }

  def apply(query: PredefinedQuery, export: QueryExport, format: String, downloadDir: Path): SpectrumDownloader =
    SpectrumDownloader(query, export, format, downloadDir, compress = true)

  /**
    * Create a download handle from a query export
    *
    * @param queryExport
    * @param format
    * @param downloadDir
    * @param compress
    * @return
    */
  def apply(queryExport: QueryExport, format: String, downloadDir: Path, compress: Boolean): SpectrumDownloader = format match {
    case "json" => new JSONDownloader(queryExport, downloadDir, compress)
    case "msp" => new MSPDownloader(queryExport, downloadDir, compress)
    case "sdf" => new SDFDownloader(queryExport, downloadDir, compress)
    case "png" => new PNGDownloader(queryExport, downloadDir, compress)
    case _ => throw new Exception(s"Unsupported format $format")
  }

  def apply(queryExport: QueryExport, format: String, downloadDir: Path): SpectrumDownloader =
    apply(queryExport, format, downloadDir, compress = true)


  /**
    * Create a download handle from query properties
    *
    * @param label
    * @param downloadDir
    * @return
    */
  def apply(label: String, query: String, format: String, downloadDir: Path, compress: Boolean): SpectrumDownloader = {
    SpectrumDownloader(
      QueryExport(UUID.randomUUID.toString, label, query, format, null, new Date, 0, 0, null, null),
      format, downloadDir, compress
    )
  }

  def apply(label: String, query: String, format: String, downloadDir: Path): SpectrumDownloader =
    apply(label, query, format, downloadDir, compress = true)
}