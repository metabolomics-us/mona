package edu.ucdavis.fiehnlab.mona.backend.services.downloader.runner.service

import java.nio.file.{Files, Path, Paths}
import javax.annotation.PostConstruct
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.domain.{PredefinedQuery, QueryExport}
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.runner.writer.SpectrumDownloader
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

import scala.collection.mutable.ArrayBuffer

/**
  * Created by sajjan on 5/25/16.
  */
@Service
@Profile(Array("mona.persistence.downloader"))
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
  def generatePredefinedExport(query: PredefinedQuery, compress: Boolean = true): PredefinedQuery = {

    val jsonDownloader: SpectrumDownloader = SpectrumDownloader(query, query.getJsonExport, "json", downloadDir, compress)
    val mspDownloader: SpectrumDownloader = SpectrumDownloader(query, query.getMspExport, "msp", downloadDir, compress)
    val sdfDownloader: SpectrumDownloader = SpectrumDownloader(query, query.getSdfExport, "sdf", downloadDir, compress)

    val downloaders: ArrayBuffer[SpectrumDownloader] = new ArrayBuffer()
    downloaders.append(jsonDownloader, mspDownloader, sdfDownloader)

    val count: Long = downloadWriterService.exportQuery(query.getQuery, query.getLabel, downloaders.toArray)

    query.setQueryCount(count)
    query.setJsonExport(jsonDownloader.toQueryExport)
    query.setMspExport(mspDownloader.toQueryExport)
    query.setSdfExport(sdfDownloader.toQueryExport)
    query
  }

  /**
    * Generate updated static exports the given query definition
    * @param compress
    */
  def generateStaticExports(export: QueryExport, compress: Boolean = true): QueryExport = {
    // Static exports (base64 spectrum-image CSV and identifier table) are disabled. They were never
    // reachable in the UI and the per-spectrum PNG rendering made a full-library run take hours,
    // which a queued request would then re-run on every downloader restart. Left as a no-op so any
    // request still sitting on the durable queue finishes instantly instead of scanning everything
    logger.info(s"static exports are disabled, skipping ${export.getLabel}")
    export.setCount(0)
    export
  }

  /**
    * Generate an export for a custom query
    * @param export
    * @return
    */
  def generateQueryExport(export: QueryExport, compress: Boolean = true): QueryExport = {
    val downloader: SpectrumDownloader = SpectrumDownloader(export, export.getFormat, downloadDir, compress)

    downloadWriterService.exportQuery(export.getQuery, export.getLabel, Array(downloader))
    downloader.toQueryExport
  }
}
