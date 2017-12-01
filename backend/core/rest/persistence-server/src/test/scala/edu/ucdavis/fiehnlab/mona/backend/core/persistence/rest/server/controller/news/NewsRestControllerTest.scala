package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.news

import java.util.Date

import com.jayway.restassured.RestAssured
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.domain.NewsEntry
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.config.{EmbeddedRestServerConfig, TestConfig}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.AbstractGenericRESTControllerTest
import org.junit.runner.RunWith
import org.scalatest.concurrent.Eventually
import org.springframework.boot.context.embedded.LocalServerPort
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner

/**
  * Created by sajjan on 4/6/17.
  */
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[EmbeddedRestServerConfig], classOf[JWTAuthenticationConfig], classOf[TestConfig]), webEnvironment = WebEnvironment.DEFINED_PORT)
class NewsRestControllerTest extends AbstractGenericRESTControllerTest[NewsEntry]("/news") with Eventually {

  @LocalServerPort
  private val port = 0

  new TestContextManager(this.getClass).prepareTestInstance(this)


  "NewsRestControllerTest" must {
    RestAssured.baseURI = s"http://localhost:$port/rest"
  }

  /**
    * object to use for gets
    *
    * @return
    */
  override def getValue: NewsEntry = NewsEntry("1", new Date(), "Test", "Test content")

  /**
    * returns an id for us for testing
    *
    * @return
    */
  override def getId: String = getValue.id
}
