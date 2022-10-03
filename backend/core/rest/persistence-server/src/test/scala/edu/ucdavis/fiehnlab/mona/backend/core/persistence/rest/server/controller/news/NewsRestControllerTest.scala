package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.news

import com.jayway.restassured.RestAssured
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.domain.News
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.AbstractGenericRESTControllerTest
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.config.{EmbeddedRestServerConfig, TestConfig}
import org.scalatest.concurrent.Eventually
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

/**
  * Created by sajjan on 4/6/17.
  */
@SpringBootTest(classes = Array(classOf[EmbeddedRestServerConfig], classOf[JWTAuthenticationConfig], classOf[TestConfig]), webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Array("test", "mona.persistence", "mona.persistence.init"))
class NewsRestControllerTest extends AbstractGenericRESTControllerTest[News, Long]("/news") with Eventually {

  @LocalServerPort
  private val port = 0

  override val deleteRequiresAuthentication: Boolean = false

  new TestContextManager(this.getClass).prepareTestInstance(this)


  "NewsRestControllerTest" must {
    RestAssured.baseURI = s"http://localhost:$port/rest"
    logger.info(s"Submitted date is: ${getValue.getSubmitted}")
  }

  /**
    * object to use for gets
    *
    * @return
    */
  def getValue: News = new News(1, "Test", "Test content")

  /**
    * returns an id for us for testing
    *
    * @return
    **/
  def getId: Long = getValue.getId

}
