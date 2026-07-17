package edu.ucdavis.fiehnlab.mona.backend.services.downloader.scheduler.controller

import com.jayway.restassured.RestAssured
import com.jayway.restassured.builder.MultiPartSpecBuilder
import com.jayway.restassured.specification.{MultiPartSpecification, RequestSpecification}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.AbstractSpringControllerTest
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.domain.StaticDownload
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.scheduler.DownloadScheduler
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.scheduler.service.StaticDownloadService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

@SpringBootTest(classes = Array(classOf[DownloadScheduler]), webEnvironment = WebEnvironment.DEFINED_PORT)
@ActiveProfiles(Array("test", "mona.persistence", "mona.persistence.init"))
class StaticDownloadControllerTest extends AbstractSpringControllerTest {

  @LocalServerPort
  private val port = 0

  @Autowired
  val staticDownloadService: StaticDownloadService = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  // RestAssured.baseURI is a single JVM-wide static also mutated by UploadSchedulerControllerTest (which
  // points it at /rest/uploads), and all suites in this module share one cached Spring context in the same
  // JVM fork, so whichever suite's assignment runs last wins for everyone; scope the base URI per request
  // instead of relying on that shared, racy static
  val baseUri: String = s"http://localhost:$port/rest/downloads"

  def given(): RequestSpecification = RestAssured.given().baseUri(baseUri)

  override def authenticate(user: String, password: String): RequestSpecification =
    super.authenticate(user, password).baseUri(baseUri)

  "StaticDownloadControllerTest" should {
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
      assert(result == new StaticDownload("monaRecord.json", null, null))
    }

    "there should be one static download available" in {
      val result: Array[StaticDownload] = given().contentType("application/json; charset=UTF-8").when().get("/static").`then`().statusCode(200).extract().body().as(classOf[Array[StaticDownload]])
      assert(result.length == 1)
      assert(result.last == new StaticDownload("monaRecord.json", null, null))
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
      assert(result == new StaticDownload("gcmsRecord.json", "test", null))
    }

    "there should be two static downloads available" in {
      val result: Array[StaticDownload] = given().contentType("application/json; charset=UTF-8").when().get("/static").`then`().statusCode(200).extract().body().as(classOf[Array[StaticDownload]])
      assert(result.length == 2)
      assert(result.contains(new StaticDownload("gcmsRecord.json", "test", null)))
    }

    "download a file with a category" in {
      given().contentType("application/json; charset=UTF-8").when().get("/static/test/gcmsRecord.json").`then`().statusCode(200)
    }
  }
}
