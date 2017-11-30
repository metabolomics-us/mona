package edu.ucdavis.fiehnlab.mona.backend.services.downloader.scheduler.controller

import com.jayway.restassured.RestAssured
import com.jayway.restassured.RestAssured.given
import com.jayway.restassured.builder.MultiPartSpecBuilder
import com.jayway.restassured.specification.MultiPartSpecification
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.AbstractSpringControllerTest
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.scheduler.DownloadScheduler
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.scheduler.service.StaticDownloadService
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[DownloadScheduler]))
class StaticDownloadControllerTest extends AbstractSpringControllerTest {

  @Autowired
  val staticDownloadService: StaticDownloadService = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "StaticDownloadControllerTest" should {
    RestAssured.baseURI = s"http://localhost:$port/rest/downloads"

    "recursively delete static download directory" in {
      staticDownloadService.removeStaticDownloadDirectory()
    }

    "return an empty array when there are no static downloads" in {
      val result: Array[StaticDownload] = given().contentType("application/json; charset=UTF-8").when().get("/static").`then`().statusCode(200).extract().body().as(classOf[Array[StaticDownload]])
      assert(result.length == 0)
    }

    "receive a 404 when trying to download a file that doesn't exist" in {
      given().contentType("application/json; charset=UTF-8").when().get("/static/doesnotexist").`then`().statusCode(404)
    }

    "upload a file with no category" in {
      val file: MultiPartSpecification = new MultiPartSpecBuilder(getClass.getResourceAsStream("/monaRecord.json"))
        .fileName("monaRecord.json")
        .controlName("file")
        .build()

      val result = authenticate().contentType("multipart/form-data").multiPart(file).when().post("/static").`then`().statusCode(200).extract().body().as(classOf[StaticDownload])
      assert(result == StaticDownload("monaRecord.json", null))
    }

    "there should be one static download available" in {
      val result: Array[StaticDownload] = given().contentType("application/json; charset=UTF-8").when().get("/static").`then`().statusCode(200).extract().body().as(classOf[Array[StaticDownload]])
      assert(result.length == 1)
      assert(result.last == StaticDownload("monaRecord.json", null))
    }

    "download a file without a category" in {
      given().contentType("application/json; charset=UTF-8").when().get("/static/monaRecord.json").`then`().statusCode(200)
    }

    "upload a file with a category" in {
      val file: MultiPartSpecification = new MultiPartSpecBuilder(getClass.getResourceAsStream("/gcmsRecord.json"))
        .fileName("gcmsRecord.json")
        .controlName("file")
        .build()

      val result = authenticate().contentType("multipart/form-data").multiPart(file).multiPart("category", "test").when().post("/static").`then`().statusCode(200).extract().body().as(classOf[StaticDownload])
      assert(result == StaticDownload("gcmsRecord.json", "test"))
    }

    "there should be two static downloads available" in {
      val result: Array[StaticDownload] = given().contentType("application/json; charset=UTF-8").when().get("/static").`then`().statusCode(200).extract().body().as(classOf[Array[StaticDownload]])
      assert(result.length == 2)
      assert(result.last == StaticDownload("gcmsRecord.json", "test"))
    }

    "download a file with a category" in {
      given().contentType("application/json; charset=UTF-8").when().get("/static/test/gcmsRecord.json").`then`().statusCode(200)
    }
  }
}
