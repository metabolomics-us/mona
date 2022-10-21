package edu.ucdavis.fiehnlab.mona.backend.services.downloader.runner.service

import java.io.InputStreamReader
import java.nio.file.{Files, Path, Paths}
import java.util.UUID
import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.dao.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.SpectrumResult
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.SpectrumResultRepository
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.{JSONDomainReader, MonaMapper}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.mat.MaterializedViewRepository
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.views.SearchTableRepository
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.domain.{PredefinedQuery, QueryExport}
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.runner.Downloader
import org.scalatest.concurrent.Eventually
import org.scalatest.wordspec.AnyWordSpec
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Properties
import java.util.Collections


/**
  * Created by sajjan on 5/26/2016.
 * */
@SpringBootTest(classes = Array(classOf[Downloader]))
@ActiveProfiles(Array("test", "mona.persistence", "mona.persistence.downloader", "mona.persistence.init"))
class SpectrumDownloaderServiceTest extends AnyWordSpec with LazyLogging with Eventually{

  @Autowired
  val downloaderService: DownloaderService = null

  @Autowired
  val spectrumResultRepository: SpectrumResultRepository = null

  @Autowired
  val matRepository: MaterializedViewRepository = null

  @Autowired
  val searchTableRepository: SearchTableRepository = null

  val objectMapper: ObjectMapper = MonaMapper.create

  @Value("${mona.downloads:#{systemProperties['java.io.tmpdir']}}#{systemProperties['file.separator']}mona_downloads")
  val dir: String = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "DownloaderServiceTest" should {
    // Populate the database
    val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")))

    "load some data" in {
      spectrumResultRepository.deleteAll()
      exampleRecords.foreach{ x =>
        spectrumResultRepository.save(new SpectrumResult(x.getId, x))
      }
    }

    s"we should be able to refresh our materialized view" in {
      eventually(timeout(180 seconds)) {
        matRepository.refreshSearchTable()
        logger.info("sleep...")
        assert(searchTableRepository.count() == 59616)
      }
    }

    // ID for
    val id: String = UUID.randomUUID().toString

    "export all spectra as JSON without compression" in {
      val export: QueryExport = new QueryExport("", "All Spectra", "", "json", null, null, 0, 0, null, null)
      val result: QueryExport = downloaderService.generateQueryExport(export, compress = false)

      assert(result.getCount == 59)
      assert(result.getSize > 0)
      assert(result.getExportFile.endsWith(".json"))
      assert(new String(Files.readAllBytes(Paths.get(dir, result.getQueryFile))).equals(export.getQuery))

      val data: Array[SpectrumResult] = JSONDomainReader.create[Array[SpectrumResult]].read(Files.newBufferedReader(Paths.get(dir, result.getExportFile)))
      assert(data.length == 59)

      Files.delete(Paths.get(dir, result.getQueryFile))
      Files.delete(Paths.get(dir, result.getExportFile))
    }

    "export all spectra as JSON with compression" in {
      val export: QueryExport = new QueryExport("", "All Spectra", "", "json", null, null, 0, 0, null, null)
      val result: QueryExport = downloaderService.generateQueryExport(export)

      assert(result.getCount == 59)
      assert(result.getSize > 0)
      assert(result.getExportFile.endsWith(".zip"))
      assert(new String(Files.readAllBytes(Paths.get(dir, result.getQueryFile))).equals(export.getQuery))

      Files.delete(Paths.get(dir, result.getQueryFile))
      Files.delete(Paths.get(dir, result.getExportFile))
    }

    "export all spectra as MSP without compression" in {
      val export: QueryExport = new QueryExport("", "All Spectra", "", "msp", null, null, 0, 0, null, null)
      val result: QueryExport = downloaderService.generateQueryExport(export, compress = false)

      assert(result.getCount == 59)
      assert(result.getSize > 0)
      assert(result.getExportFile.endsWith(".msp"))
      assert(new String(Files.readAllBytes(Paths.get(dir, result.getQueryFile))).equals(export.getQuery))

      val data: Array[String] = new String(Files.readAllBytes(Paths.get(dir, result.getExportFile))).split("\n\n")
      assert(data.length == 59)

      Files.delete(Paths.get(dir, result.getQueryFile))
      Files.delete(Paths.get(dir, result.getExportFile))
    }

    "export all spectra as MSP with compression" in {
      val export: QueryExport = new QueryExport("", "All Spectra", "", "msp", null, null, 0, 0, null, null)
      val result: QueryExport = downloaderService.generateQueryExport(export)

      assert(result.getCount == 59)
      assert(result.getSize > 0)
      assert(result.getExportFile.endsWith(".zip"))
      assert(new String(Files.readAllBytes(Paths.get(dir, result.getQueryFile))).equals(export.getQuery))

      Files.delete(Paths.get(dir, result.getQueryFile))
      Files.delete(Paths.get(dir, result.getExportFile))
    }

    "export negative mode spectra as JSON" in {
      val export: QueryExport = new QueryExport("", "Negative Mode Spectra", "metadataName==\'ion mode\' and metadataValue==\'negative\'", "json", null, null, 0, 0, null, null)
      val result: QueryExport = downloaderService.generateQueryExport(export, compress = false)

      assert(result.getCount == 25)
      assert(result.getSize > 0)
      assert(result.getExportFile.endsWith(".json"))
      assert(new String(Files.readAllBytes(Paths.get(dir, result.getQueryFile))).equals(export.getQuery))

      val data: Array[SpectrumResult] = JSONDomainReader.create[Array[SpectrumResult]].read(Files.newBufferedReader(Paths.get(dir, result.getExportFile)))
      assert(data.length == 25)

      Files.delete(Paths.get(dir, result.getQueryFile))
      Files.delete(Paths.get(dir, result.getExportFile))
    }

    "export negative mode spectra as MSP" in {
      val export: QueryExport = new QueryExport("", "Negative Mode Spectra", "metadataName==\'ion mode\' and metadataValue==\'negative\'", "msp", null, null, 0, 0, null, null)
      val result: QueryExport = downloaderService.generateQueryExport(export, compress = false)

      assert(result.getCount == 25)
      assert(result.getSize > 0)
      assert(result.getExportFile.endsWith(".msp"))
      assert(new String(Files.readAllBytes(Paths.get(dir, result.getQueryFile))).equals(export.getQuery))

      val data: Array[String] = new String(Files.readAllBytes(Paths.get(dir, result.getExportFile))).split("\n\n")
      assert(data.length == 25)

      Files.delete(Paths.get(dir, result.getQueryFile))
      Files.delete(Paths.get(dir, result.getExportFile))
    }

    "export predefined query for all spectra" in {
      val query: PredefinedQuery = new PredefinedQuery("All Spectra", "", "", 0, null, null, null)
      val result: PredefinedQuery = downloaderService.generatePredefinedExport(query, compress = false, enableAllSpectraStaticFiles = true)

      assert(result.getQueryCount == 59)

      Array(result.getJsonExport, result.getMspExport, result.getSdfExport).foreach(export => {
        assert(export.getCount == 59)
        assert(export.getSize > 0)
        assert(Files.exists(Paths.get(dir, export.getExportFile)))
        assert(Files.exists(Paths.get(dir, export.getQueryFile)))

        Files.delete(Paths.get(dir, export.getExportFile))
      })

      Files.delete(Paths.get(dir, result.getJsonExport.getQueryFile))


      // Check that png export was created
      val pngFile: Path = Paths.get(dir, "static", result.getJsonExport.getExportFile.replace(".json", "-spectrum-images.csv"))
      val pngDescriptionFile: Path = Paths.get(dir, "static", result.getJsonExport.getExportFile.replace(".json", "-spectrum-images.csv") +".description.txt")

      assert(Files.exists(pngFile))
      assert(Files.exists(pngDescriptionFile))
      assert(new String(Files.readAllBytes(pngFile)).trim.split("\n").length == 59)

      Files.delete(pngFile)
      Files.delete(pngDescriptionFile)

      // Check that identifier table was created
      val idsFile: Path = Paths.get(dir, "static", result.getJsonExport.getExportFile.replace(".json", "-identifier-table.csv"))
      val idsDescriptionFile: Path = Paths.get(dir, "static", result.getJsonExport.getExportFile.replace(".json", "-identifier-table.csv") +".description.txt")

      assert(Files.exists(idsFile))
      assert(Files.exists(idsDescriptionFile))
      assert(new String(Files.readAllBytes(idsFile)).trim.split("\n").length == 59)

      Files.delete(idsFile)
      Files.delete(idsDescriptionFile)
    }

    "export predefined query for query" in {
      val query: PredefinedQuery = new PredefinedQuery("Negative Mode Spectra", "", "metadataName==\'ion mode\' and metadataValue==\'negative\'", 0, null, null, null)
      val result: PredefinedQuery = downloaderService.generatePredefinedExport(query, compress = false, enableAllSpectraStaticFiles = true)

      assert(result.getQueryCount == 25)

      Array(result.getJsonExport, result.getMspExport, result.getSdfExport).foreach(export => {
        assert(export.getCount == 25)
        assert(export.getSize > 0)
        assert(Files.exists(Paths.get(dir, export.getExportFile)))
        assert(Files.exists(Paths.get(dir, export.getQueryFile)))

        Files.delete(Paths.get(dir, export.getExportFile))
      })

      Files.delete(Paths.get(dir, result.getJsonExport.getQueryFile))

      // Check that png export was NOT created
      val pngFile: Path = Paths.get(dir, "static", result.getJsonExport.getExportFile.replace(".json", "-spectrum-images.csv"))
      assert(Files.notExists(pngFile))
    }
  }
}
