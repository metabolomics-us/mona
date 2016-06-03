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
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.QueryDownloader
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.config.QueryDownloaderConfig
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.context.annotation.{Import, Bean, Configuration}
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
  * Created by sajjan on 6/2/2016.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[QueryDownloader]))
class QueryDownloaderControllerTest extends AbstractSpringControllerTest {

//  @Autowired
//  val mongoRepository: ISpectrumMongoRepositoryCustom = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "QueryDownloaderControllerTest" should {

    RestAssured.baseURI = s"http://localhost:$port"

    "these must all fail, since we require to be logged in " must {
      "scheduleByQuery" in {
        given().contentType("application/json; charset=UTF-8").when().get("/rest/downloads/schedule?query=metaData=q='name==\"ion mode\" and value==negative'").then().statusCode(401)
      }
    }

    "these must all pass, since we are logged in " must {
      "scheduleByQuery by user" in {
        val result = authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").when().get("/rest/downloads/schedule?query=metaData=q='name==\"ion mode\" and value==negative'").then().statusCode(200).extract().body().as(classOf[QueryDownloadJobScheduled])
        assert(result.count == 101)
      }

      "scheduleByQuery by admin" in {
        val result = authenticate().contentType("application/json; charset=UTF-8").when().get("/rest/downloads/schedule?query=metaData=q='name==\"ion mode\" and value==negative'").then().statusCode(200).extract().body().as(classOf[QueryDownloadJobScheduled])
        assert(result.count == 101)
      }
    }

    "these must all fail, since we require a query to be passed " must {
      "scheduleByQuery" in {
        authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").when().get("/schedule").then().statusCode(400)
        authenticate().contentType("application/json; charset=UTF-8").when().get("/rest/downloads/schedule").then().statusCode(400)
      }
    }

    "if specified the server should stay online, this can be done using the env variable 'keep.server.running=true' " in {
        logger.warn("waiting forever till you kill me!")
        Thread.sleep(300000); // Every 5 minutes
    }
  }
}

//@Configuration
//@Import(Array(classOf[QueryDownloaderConfig]))
//class TestConfig {
//
//  /**
//    * the service which actually does the login for us
//    *
//    * @return
//    */
//  @Bean
//  def loginService: LoginService = new MongoLoginService
//}