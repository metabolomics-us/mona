package edu.ucdavis.fiehnlab.mona.backend.services.downloader.service

import java.io.InputStreamReader
import java.nio.file.{Files, Paths}

import com.jayway.restassured.RestAssured
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.api.MonaSpectrumRestClient
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.config.RestClientTestConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.AbstractSpringControllerTest
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.Downloader
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.types.QueryExport
import org.junit.runner.RunWith
import org.scalatest.concurrent.Eventually
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
  * Created by sajjan on 5/26/2016.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[Downloader], classOf[RestClientTestConfig]))
class DownloaderServiceTest extends AbstractSpringControllerTest with Eventually {

  @Autowired
  val downloaderService: DownloaderService = null

  @Autowired
  val restClient: MonaSpectrumRestClient = null

  @Value("${mona.export.path:#{systemProperties['java.io.tmpdir']}}#{systemProperties['file.separator']}mona_exports")
  val dir: String = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "DownloaderServiceTest" should {
    // Populate the database
    val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")))



    val export: QueryExport = QueryExport("test", "", "json", 0, 0, null)
    downloaderService.download(export)

    "export the query file" in {
      assert(Files.exists(Paths.get(dir, "test-query.txt")))
      assert(new String(Files.readAllBytes(Paths.get(dir, "test-query.txt"))).equals(export.query))
    }
  }
}