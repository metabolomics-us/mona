package edu.ucdavis.fiehnlab.mona.backend.services.downloader.runner.service

import java.nio.file.{Files, Path, Paths}
import javax.annotation.PostConstruct

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.core.types.{PredefinedQuery, QueryExport}
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.runner.writer.SpectrumDownloader
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.stereotype.Service

import scala.collection.mutable.ArrayBuffer

/**
  * Created by sajjan on 5/25/16.
  */
@Service
class DownloaderService extends LazyLogging {

  @Value("${mona.downloads:#{systemProperties['java.io.tmpdir']}}#{systemProperties['file.separator']}mona_downloads")
  private val downloadDirPath: String = null

  private def downloadDir: Path = Paths.get(downloadDirPath)

  private def staticDownloadDir: Path = Paths.get(downloadDirPath, "static")

  @Autowired
  private val downloadWriterService: DownloadWriterService = null

  /**
    * Create export directories if needed
    */
  @PostConstruct
  private def init(): Unit = {
    if (Files.notExists(downloadDir)) {
      Files.createDirectories(downloadDir)
    }

    if (Files.notExists(staticDownloadDir)) {
      Files.createDirectories(staticDownloadDir)
    }
  }

  /**
    * Generate updated exports for a predefined query definition
    * @param query
    * @return
    */
  def generatePredefinedExport(query: PredefinedQuery, compress: Boolean = true, enableAllSpectraStaticFiles: Boolean = false): PredefinedQuery = {

    val jsonDownloader: SpectrumDownloader = SpectrumDownloader(query, query.jsonExport, "json", downloadDir, compress)
    val mspDownloader: SpectrumDownloader = SpectrumDownloader(query, query.mspExport, "msp", downloadDir, compress)
    val sdfDownloader: SpectrumDownloader = SpectrumDownloader(query, query.sdfExport, "sdf", downloadDir, compress)

    val downloaders: ArrayBuffer[SpectrumDownloader] = new ArrayBuffer()
    downloaders.append(jsonDownloader, mspDownloader, sdfDownloader)

    // Create additional static files if this query corresponds to all spectra
    if (enableAllSpectraStaticFiles && query.query.isEmpty) {
      downloaders.append(SpectrumDownloader(query.label, query.query, "png", staticDownloadDir, compress))
      downloaders.append(SpectrumDownloader(query.label, query.query, "ids", staticDownloadDir, compress))
    }

    val count: Long = downloadWriterService.exportQuery(query.query, query.label, downloaders.toArray)

    query.copy(
      queryCount = count,
      jsonExport = jsonDownloader.toQueryExport,
      mspExport = mspDownloader.toQueryExport,
      sdfExport = sdfDownloader.toQueryExport
    )
  }

  /**
    * Generate updated static exports the given query definition
    * @param compress
    */
  def generateStaticExports(export: QueryExport, compress: Boolean = true): QueryExport = {
    val downloaders = Array(
      SpectrumDownloader(export.label, export.query, "png", staticDownloadDir, compress),
      SpectrumDownloader(export.label, export.query, "ids", staticDownloadDir, compress)
    )

    downloadWriterService.exportQuery(export.query, export.label, downloaders)
    export.copy(count = downloaders.head.toQueryExport.count)
  }

  /**
    * Generate an export for a custom query
    * @param export
    * @return
    */
  def generateQueryExport(export: QueryExport, compress: Boolean = true): QueryExport = {
    val downloader: SpectrumDownloader = SpectrumDownloader(export, export.format, downloadDir, compress)

    downloadWriterService.exportQuery(export.query, export.label, Array(downloader))
    downloader.toQueryExport
  }
}
