package edu.ucdavis.fiehnlab.mona.backend.services.downloader.scheduler.controller

import java.io.InputStreamReader
import java.util.Date
import com.jayway.restassured.RestAssured
import com.jayway.restassured.RestAssured._
import edu.ucdavis.fiehnlab.mona.backend.core.domain.dao.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.SpectrumResult
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.SpectrumResultRepository
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.AbstractSpringControllerTest
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.StatisticsTagRepository
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.mat.MaterializedViewRepository
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.views.SearchTableRepository
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.core.repository.{PredefinedQueryRepository, QueryExportRepository}
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.domain.{PredefinedQuery, QueryExport}
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.scheduler.DownloadScheduler
import org.scalatest.concurrent.Eventually
import org.scalatest.matchers.should.Matchers
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

import scala.concurrent.duration._
import scala.language.postfixOps


/**
  * Created by sajjan on 5/26/16.
  */
@SpringBootTest(classes = Array(classOf[DownloadScheduler]), webEnvironment = WebEnvironment.DEFINED_PORT)
@ActiveProfiles(Array("test", "mona.persistence", "mona.persistence.init"))
class DownloadSchedulerControllerTest extends AbstractSpringControllerTest with Matchers with Eventually{

  @LocalServerPort
  private val port = 0

  @Autowired
  val spectrumResultRepository: SpectrumResultRepository = null

  @Autowired
  val queryExportRepository: QueryExportRepository = null

  @Autowired
  val predefinedQueryRepository: PredefinedQueryRepository = null

  @Autowired
  private val tagStatisticsRepository: StatisticsTagRepository = null

  @Autowired
  val matRepository: MaterializedViewRepository = null

  @Autowired
  val searchTableRepository: SearchTableRepository = null


  new TestContextManager(this.getClass).prepareTestInstance(this)

  "DownloadSchedulerControllerTest" should {
    RestAssured.baseURI = s"http://localhost:$port/rest/downloads"

    // Populate the database
    val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")))

    "load some data" in {
      spectrumResultRepository.deleteAll()
      queryExportRepository.deleteAll()
      predefinedQueryRepository.deleteAll()
      tagStatisticsRepository.deleteAll()

      exampleRecords.foreach{ x =>
        spectrumResultRepository.save(new SpectrumResult(x.getId, x))
      }
      assert(spectrumResultRepository.count() == exampleRecords.length)

      queryExportRepository.save(new QueryExport("test", "test", "metaData=q='name==\"ion mode\" and value==negative'", "json", "test", new Date, 0, 0, null, null))
      assert(queryExportRepository.count() == 1)
    }

    s"we should be able to refresh our materialized view" in {
      eventually(timeout(180 seconds)) {
        matRepository.refreshSearchTable()
        logger.info("sleep...")
        assert(searchTableRepository.count() == 59610)
      }
    }

    // Test download of a spectrum
    "download" must {
      "return an error if the download does not exist" in {
        given().contentType("application/json; charset=UTF-8").when().get("/retrieve/doesnotexist").`then`().statusCode(404)
      }
    }

    // Add new predefined download
    "add predefined download" in {
      val query: PredefinedQuery = predefinedQueryRepository.save(new PredefinedQuery("All Spectra", "", "", 0, null, null, null))
      assert(predefinedQueryRepository.count() == 1)

      given().contentType("application/json; charset=UTF-8").when().body(query).post("/predefined").`then`().statusCode(401)

      val result = authenticate().contentType("application/json; charset=UTF-8").when().body(query).post("/predefined").`then`().statusCode(200).extract().body().as(classOf[PredefinedQuery])

      assert(query == result)
    }

    // List predefined downloads
    "list predefined downloads" in {
      val result = given().contentType("application/json; charset=UTF-8").when().get("/predefined").`then`().statusCode(200).extract().body().as(classOf[Array[PredefinedQuery]])

      assert(result.length == 1)
      assert(result.head.getLabel == "All Spectra")
    }

    // Test scheduling of query
    "schedule a download " must {
      "fail if not authenticated" in {
        given().contentType("application/json; charset=UTF-8").when().get("/schedule?query=metadataName==\'ion mode\' and metadataValue==\'negative\'").`then`().statusCode(401)
      }

      "fail if authenticated but do not provide a query" in {
        authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").when().get("/schedule").`then`().statusCode(400)
      }

      "fail if authenticated as an admin but do not provide a query" in {
        authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").when().get("/schedule").`then`().statusCode(400)
      }

      "succeed if authenticated" in {
        val result = authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").when().get("/schedule?query=metadataName==\'ion mode\' and metadataValue==\'negative\'").`then`().statusCode(200).extract().body().as(classOf[QueryExport])
        assert(result.getId.matches("^[0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12}$"))
      }

      "succeed if authenticated as an admin" in {
        val result = authenticate().contentType("application/json; charset=UTF-8").when().get("/schedule?query=metadataName==\'ion mode\' and metadataValue==\'negative\'").`then`().statusCode(200).extract().body().as(classOf[QueryExport])
        assert(result.getId.matches("^[0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12}$"))
      }

      "reschedule download" in {
        authenticate().contentType("application/json; charset=UTF-8").when().get("/schedule/test").`then`().statusCode(200)
      }
    }

    // Test scheduling of predefined downloads
    "generate predefined downloads " must {
      "fail if not authenticated" in {
        given().contentType("application/json; charset=UTF-8").when().get("/generatePredefined").`then`().statusCode(401)
      }

      "fail if authenticated as a user" in {
        authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").when().get("/generatePredefined").`then`().statusCode(403)
      }

      "succeed if authenticated as an admin" in {
        val result = authenticate().contentType("application/json; charset=UTF-8").when().get("/generatePredefined").`then`().statusCode(200).extract().body().as(classOf[Array[QueryExport]])

        assert(result.length == 1)
        assert(result.exists(_.getLabel == "All Spectra"))
      }
    }
  }
}
