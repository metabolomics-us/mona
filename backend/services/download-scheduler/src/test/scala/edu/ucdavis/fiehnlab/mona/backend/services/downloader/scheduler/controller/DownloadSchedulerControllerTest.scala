package edu.ucdavis.fiehnlab.mona.backend.services.downloader.scheduler.controller

import java.io.InputStreamReader
import java.util.Date

import com.jayway.restassured.RestAssured
import com.jayway.restassured.RestAssured._
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.ISpectrumMongoRepositoryCustom
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.AbstractSpringControllerTest
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.core.repository.{PredefinedQueryMongoRepository, QueryExportMongoRepository}
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.core.types.{PredefinedQuery, QueryExport}
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.scheduler.DownloadScheduler
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
  * Created by sajjan on 5/26/16.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[DownloadScheduler]))
class DownloadSchedulerControllerTest extends AbstractSpringControllerTest with Matchers {

  @Autowired
  val mongoRepository: ISpectrumMongoRepositoryCustom = null

  @Autowired
  val queryExportRepository: QueryExportMongoRepository = null

  @Autowired
  val predefinedQueryRepository: PredefinedQueryMongoRepository = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "DownloadSchedulerControllerTest" should {
    RestAssured.baseURI = s"http://localhost:$port/rest/downloads"

    // Populate the database
    val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")))

    "load some data" in {
      mongoRepository.deleteAll()
      queryExportRepository.deleteAll()
      predefinedQueryRepository.deleteAll()

      for (spectrum <- exampleRecords) {
        mongoRepository.save(spectrum)
      }

      queryExportRepository.save(QueryExport("test", "test", "metaData=q='name==\"ion mode\" and value==negative'", "json", "test", new Date, 0, 0, null, null))
    }

    // Test download of a spectrum
    "download" must {
      "return an error if the download does not exist" in {
        given().contentType("application/json; charset=UTF-8").when().get("/retrieve/doesnotexist").`then`().statusCode(404)
      }
    }

    // Add new predefined download
    "add predefined download" in {
      val query: PredefinedQuery = predefinedQueryRepository.save(PredefinedQuery("All Spectra", "", "", 0, null, null))

      given().contentType("application/json; charset=UTF-8").when().body(query).post("/predefined").`then`().statusCode(401)

      val result = authenticate().contentType("application/json; charset=UTF-8").when().body(query).post("/predefined").`then`().statusCode(200).extract().body().as(classOf[PredefinedQuery])

      assert(query == result)
    }

    // List predefined downloads
    "list predefined downloads" in {
      val result = given().contentType("application/json; charset=UTF-8").when().get("/predefined").`then`().statusCode(200).extract().body().as(classOf[Array[PredefinedQuery]])

      assert(result.length == 1)
      assert(result.head.label == "All Spectra")
    }

    // Test scheduling of query
    "schedule a download " must {
      "fail if not authenticated" in {
        given().contentType("application/json; charset=UTF-8").when().get("/schedule?query=metaData=q='name==\"ion mode\" and value==negative'").`then`().statusCode(401)
      }

      "fail if authenticated but do not provide a query" in {
        authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").when().get("/schedule").`then`().statusCode(400)
      }

      "fail if authenticated as an admin but do not provide a query" in {
        authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").when().get("/schedule").`then`().statusCode(400)
      }

      "succeed if authenticated" in {
        val result = authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").when().get("/schedule?query=metaData=q='name==\"ion mode\" and value==negative'").`then`().statusCode(200).extract().body().as(classOf[QueryExport])
        assert(result.id.matches("^[0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12}$"))
      }

      "succeed if authenticated as an admin" in {
        val result = authenticate().contentType("application/json; charset=UTF-8").when().get("/schedule?query=metaData=q='name==\"ion mode\" and value==negative'").`then`().statusCode(200).extract().body().as(classOf[QueryExport])
        assert(result.id.matches("^[0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12}$"))
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

        assert(result.length == 2)
        assert(result.forall(_.id.matches("^[0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12}$")))
        assert(result.exists(_.label == "All Spectra"))
      }
    }
  }
}