package edu.ucdavis.fiehnlab.mona.backend.services.downloader.controller

import com.jayway.restassured.RestAssured
import com.jayway.restassured.RestAssured._
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.service.MongoLoginService
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.AbstractSpringControllerTest
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.Downloader
import org.junit.runner.RunWith
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
  * Created by sajjan on 5/26/16.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[Downloader]))
class DownloadControllerTest extends AbstractSpringControllerTest {

  new TestContextManager(this.getClass).prepareTestInstance(this)

  // Test query
  val testQuery: String = "metaData=q='name==\"ion mode\" and value==negative'"


  "DownloadControllerTest" should {
    RestAssured.baseURI = s"http://localhost:$port/rest"

    "these must all fail, since we require to be logged in " must {
      "schedule" in {
        given().contentType("application/json; charset=UTF-8").when().get(s"/downloads/schedule?query=$testQuery").then().statusCode(401)
      }

      "these must all pass, since we are logged in " must {
        "schedule by user" in {
          val result = authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").when().get(s"/downloads/schedule?query=$testQuery").then().statusCode(200).extract().body().as(classOf[DownloadJobScheduled])
          assert(result.id == testQuery)
        }

        "scheduleByQuery by admin" in {
          val result = authenticate().contentType("application/json; charset=UTF-8").when().get(s"/downloads/schedule?query=$testQuery").then().statusCode(200).extract().body().as(classOf[DownloadJobScheduled])
          assert(result.id == testQuery)
        }
      }

      "these must all fail, since we require a query to be passed " must {
        "scheduleByQuery" in {
          authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").when().get("/downloads/schedule").then().statusCode(400)
          authenticate().contentType("application/json; charset=UTF-8").when().get("/downloads/schedule").then().statusCode(400)
        }
      }
    }
  }
}

@Configuration
class TestConfig {

  @Bean
  def loginService: LoginService = new MongoLoginService
}