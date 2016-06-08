package edu.ucdavis.fiehnlab.mona.backend.services.downloader.controller

import com.jayway.restassured.RestAssured
import com.jayway.restassured.RestAssured._
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.AbstractSpringControllerTest
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.DownloadScheduler
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.service.ScheduledDownload
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
  * Created by sajjan on 5/26/16.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[DownloadScheduler]))
class DownloadSchedulerControllerTest extends AbstractSpringControllerTest with Matchers {

  new TestContextManager(this.getClass).prepareTestInstance(this)

  // Test query
  val testQuery: String = "metaData=q='name==\"ion mode\" and value==negative'"


  "DownloadControllerTest" should {
    RestAssured.baseURI = s"http://localhost:$port/rest"

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
        val result = authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").when().get(s"/downloads/schedule?query=$testQuery").then().statusCode(200).extract().body().as(classOf[ScheduledDownload])
        assert(result.id.matches("^[0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12}$"))
      }

      "succeed if authenticated as an admin" in {
        val result = authenticate().contentType("application/json; charset=UTF-8").when().get(s"/downloads/schedule?query=$testQuery").then().statusCode(200).extract().body().as(classOf[ScheduledDownload])
        assert(result.id.matches("^[0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12}$"))
      }
    }

    "schedulinh predefined downloads " must {
      "fail if not authenticated" in {
        given().contentType("application/json; charset=UTF-8").when().get("/downloads/schedulePredefinedDownloads").then().statusCode(401)
      }

      "fail if authenticated as a user" in {
        authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").when().get("/downloads/schedulePredefinedDownloads").then().statusCode(403)
      }

      "succeed if authenticated as an admin" in {
        val result = authenticate().contentType("application/json; charset=UTF-8").when().get("/downloads/schedulePredefinedDownloads").then().statusCode(200).extract().body().as(classOf[ScheduledDownload])
        assert(result.id.matches("^[0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12}$"))
      }
    }
  }
}