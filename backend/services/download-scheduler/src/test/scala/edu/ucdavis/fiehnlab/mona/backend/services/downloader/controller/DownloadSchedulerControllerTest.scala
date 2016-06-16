package edu.ucdavis.fiehnlab.mona.backend.services.downloader.controller

import java.io.InputStreamReader

import com.jayway.restassured.RestAssured
import com.jayway.restassured.RestAssured._
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.service.MongoLoginService
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.ISpectrumMongoRepositoryCustom
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.AbstractSpringControllerTest
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.DownloadScheduler
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.repository.PredefinedQueryMongoRepository
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.types.{PredefinedQuery, QueryExport}
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
  val predefinedQueryRepository: PredefinedQueryMongoRepository = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "DownloadControllerTest" should {
    RestAssured.baseURI = s"http://localhost:$port/rest"

    // Populate the database
    val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")))

    "load some data" in {
      mongoRepository.deleteAll()

      for (spectrum <- exampleRecords) {
        mongoRepository.save(spectrum)
      }

      predefinedQueryRepository.deleteAll()
      predefinedQueryRepository.save(PredefinedQuery("All Spectra", "", "", 0, null, null))
    }

    // Test scheduling of query
    "scheduling a download " must {
      "fail if not authenticated" in {
        given().contentType("application/json; charset=UTF-8").when().get("/downloads/schedule?query=metaData=q='name==\"ion mode\" and value==negative'").then().statusCode(401)
      }

      "fail if authenticated but do not provide a query" in {
        authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").when().get("/downloads/schedule").then().statusCode(400)
      }

      "fail if authenticated as an admin but do not provide a query" in {
        authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").when().get("/downloads/schedule").then().statusCode(400)
      }

      "succeed if authenticated" in {
        val result = authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").when().get("/downloads/schedule?query=metaData=q='name==\"ion mode\" and value==negative'").then().statusCode(200).extract().body().as(classOf[QueryExport])
        assert(result.id.matches("^[0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12}$"))
      }

      "succeed if authenticated as an admin" in {
        val result = authenticate().contentType("application/json; charset=UTF-8").when().get("/downloads/schedule?query=metaData=q='name==\"ion mode\" and value==negative'").then().statusCode(200).extract().body().as(classOf[QueryExport])
        assert(result.id.matches("^[0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12}$"))
      }
    }

    // Test scheduling of predefined downloads
    "scheduling predefined downloads " must {
      "fail if not authenticated" in {
        given().contentType("application/json; charset=UTF-8").when().get("/downloads/schedulePredefinedDownloads").then().statusCode(401)
      }

      "fail if authenticated as a user" in {
        authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").when().get("/downloads/schedulePredefinedDownloads").then().statusCode(403)
      }

      "succeed if authenticated as an admin" in {
        val result = authenticate().contentType("application/json; charset=UTF-8").when().get("/downloads/schedulePredefinedDownloads").then().statusCode(200).extract().body().as(classOf[Array[QueryExport]])

        assert(result.length == 2)
        assert(result.forall(x => x.id.matches("^[0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12}$")))
        assert(result.forall(x => x.label == "All Spectra"))
      }
    }
  }
}