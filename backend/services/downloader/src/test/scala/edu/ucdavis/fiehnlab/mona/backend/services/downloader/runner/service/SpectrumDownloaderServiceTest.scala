package edu.ucdavis.fiehnlab.mona.backend.services.downloader.runner.service

import java.io.InputStreamReader
import java.nio.file.{Files, Path, Paths}
import java.util.UUID

import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.{JSONDomainReader, MonaMapper}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.ISpectrumMongoRepositoryCustom
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.core.types.{PredefinedQuery, QueryExport}
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.runner.Downloader
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner


/**
  * Created by sajjan on 5/26/2016.
  */
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[Downloader]))
class SpectrumDownloaderServiceTest extends WordSpec with LazyLogging {

  @Autowired
  val downloaderService: DownloaderService = null

  @Autowired
  val mongoRepository: ISpectrumMongoRepositoryCustom = null

  val objectMapper: ObjectMapper = MonaMapper.create

  @Value("${mona.downloads:#{systemProperties['java.io.tmpdir']}}#{systemProperties['file.separator']}mona_downloads")
  val dir: String = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "DownloaderServiceTest" should {
    // Populate the database
    val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")))

    "load some data" in {
      mongoRepository.deleteAll()
      exampleRecords.foreach(mongoRepository.save(_))
    }

    // ID for
    val id: String = UUID.randomUUID().toString

    "export all spectra as JSON without compression" in {
      val export: QueryExport = QueryExport("", "All Spectra", "", "json", null, null, 0, 0, null, null)
      val result: QueryExport = downloaderService.downloadQueryExport(export, compress = false)

      assert(result.count == 58)
      assert(result.size > 0)
      assert(result.exportFile.endsWith(".json"))
      assert(new String(Files.readAllBytes(Paths.get(dir, result.queryFile))).equals(export.query))

      val data: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(Files.newBufferedReader(Paths.get(dir, result.exportFile)))
      assert(data.length == 58)

      Files.delete(Paths.get(dir, result.queryFile))
      Files.delete(Paths.get(dir, result.exportFile))
    }

    "export all spectra as JSON with compression" in {
      val export: QueryExport = QueryExport("", "All Spectra", "", "json", null, null, 0, 0, null, null)
      val result: QueryExport = downloaderService.downloadQueryExport(export)

      assert(result.count == 58)
      assert(result.size > 0)
      assert(result.exportFile.endsWith(".zip"))
      assert(new String(Files.readAllBytes(Paths.get(dir, result.queryFile))).equals(export.query))

      Files.delete(Paths.get(dir, result.queryFile))
      Files.delete(Paths.get(dir, result.exportFile))
    }

    "export all spectra as MSP without compression" in {
      val export: QueryExport = QueryExport("", "All Spectra", "", "msp", null, null, 0, 0, null, null)
      val result: QueryExport = downloaderService.downloadQueryExport(export, compress = false)

      assert(result.count == 58)
      assert(result.size > 0)
      assert(result.exportFile.endsWith(".msp"))
      assert(new String(Files.readAllBytes(Paths.get(dir, result.queryFile))).equals(export.query))

      val data: Array[String] = new String(Files.readAllBytes(Paths.get(dir, result.exportFile))).split("\n\n")
      assert(data.length == 58)

      Files.delete(Paths.get(dir, result.queryFile))
      Files.delete(Paths.get(dir, result.exportFile))
    }

    "export all spectra as MSP with compression" in {
      val export: QueryExport = QueryExport("", "All Spectra", "", "msp", null, null, 0, 0, null, null)
      val result: QueryExport = downloaderService.downloadQueryExport(export)

      assert(result.count == 58)
      assert(result.size > 0)
      assert(result.exportFile.endsWith(".zip"))
      assert(new String(Files.readAllBytes(Paths.get(dir, result.queryFile))).equals(export.query))

      Files.delete(Paths.get(dir, result.queryFile))
      Files.delete(Paths.get(dir, result.exportFile))
    }

    "export negative mode spectra as JSON" in {
      val export: QueryExport = QueryExport("", "Negative Mode Spectra", "metaData=q='name==\"ion mode\" and value==negative'", "json", null, null, 0, 0, null, null)
      val result: QueryExport = downloaderService.downloadQueryExport(export, compress = false)

      assert(result.count == 25)
      assert(result.size > 0)
      assert(result.exportFile.endsWith(".json"))
      assert(new String(Files.readAllBytes(Paths.get(dir, result.queryFile))).equals(export.query))

      val data: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(Files.newBufferedReader(Paths.get(dir, result.exportFile)))
      assert(data.length == 25)

      Files.delete(Paths.get(dir, result.queryFile))
      Files.delete(Paths.get(dir, result.exportFile))
    }

    "export negative mode spectra as MSP" in {
      val export: QueryExport = QueryExport("", "Negative Mode Spectra", "metaData=q='name==\"ion mode\" and value==negative'", "msp", null, null, 0, 0, null, null)
      val result: QueryExport = downloaderService.downloadQueryExport(export, compress = false)

      assert(result.count == 25)
      assert(result.size > 0)
      assert(result.exportFile.endsWith(".msp"))
      assert(new String(Files.readAllBytes(Paths.get(dir, result.queryFile))).equals(export.query))

      val data: Array[String] = new String(Files.readAllBytes(Paths.get(dir, result.exportFile))).split("\n\n")
      assert(data.length == 25)

      Files.delete(Paths.get(dir, result.queryFile))
      Files.delete(Paths.get(dir, result.exportFile))
    }

    "export predefined query for all spectra" in {
      val query: PredefinedQuery = PredefinedQuery("All Spectra", "", "", 0, null, null, null)
      val result: PredefinedQuery = downloaderService.downloadPredefinedQuery(query, compress = false, enableAllSpectraStaticFiles = true)

      assert(result.queryCount == 58)

      Array(result.jsonExport, result.mspExport, result.sdfExport).foreach(export => {
        assert(export.count == 58)
        assert(export.size > 0)
        assert(Files.exists(Paths.get(dir, export.exportFile)))
        assert(Files.exists(Paths.get(dir, export.queryFile)))

        Files.delete(Paths.get(dir, export.exportFile))
      })

      Files.delete(Paths.get(dir, result.jsonExport.queryFile))


      // Check that png export was created
      val pngFile: Path = Paths.get(dir, "static", result.jsonExport.exportFile.replace(".json", "-spectrum-images.csv"))
      val pngDescriptionFile: Path = Paths.get(dir, "static", result.jsonExport.exportFile.replace(".json", "-spectrum-images.csv") +".description.txt")

      assert(Files.exists(pngFile))
      assert(Files.exists(pngDescriptionFile))
      assert(new String(Files.readAllBytes(pngFile)).trim.split("\n").length == 58)

      Files.delete(pngFile)
      Files.delete(pngDescriptionFile)

      // Check that identifier table was created
      val idsFile: Path = Paths.get(dir, "static", result.jsonExport.exportFile.replace(".json", "-identifier-table.csv"))
      val idsDescriptionFile: Path = Paths.get(dir, "static", result.jsonExport.exportFile.replace(".json", "-identifier-table.csv") +".description.txt")

      assert(Files.exists(idsFile))
      assert(Files.exists(idsDescriptionFile))
      assert(new String(Files.readAllBytes(idsFile)).trim.split("\n").length == 58)

      Files.delete(idsFile)
      Files.delete(idsDescriptionFile)
    }

    "export predefined query for query" in {
      val query: PredefinedQuery = PredefinedQuery("Negative Mode Spectra", "", "metaData=q='name==\"ion mode\" and value==negative'", 0, null, null, null)
      val result: PredefinedQuery = downloaderService.downloadPredefinedQuery(query, compress = false, enableAllSpectraStaticFiles = true)

      assert(result.queryCount == 25)

      Array(result.jsonExport, result.mspExport, result.sdfExport).foreach(export => {
        assert(export.count == 25)
        assert(export.size > 0)
        assert(Files.exists(Paths.get(dir, export.exportFile)))
        assert(Files.exists(Paths.get(dir, export.queryFile)))

        Files.delete(Paths.get(dir, export.exportFile))
      })

      Files.delete(Paths.get(dir, result.jsonExport.queryFile))

      // Check that png export was NOT created
      val pngFile: Path = Paths.get(dir, "static", result.jsonExport.exportFile.replace(".json", "-spectrum-images.csv"))
      assert(Files.notExists(pngFile))
    }
  }
}