package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.spectrum

import com.jayway.restassured.RestAssured
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.domain.SpectrumComment
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.config.{EmbeddedRestServerConfig, TestConfig}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.AbstractGenericRESTControllerTest
import org.junit.runner.RunWith
import org.scalatest.concurrent.Eventually
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner

/**
  * Created by sajjan on 4/6/17.
  */
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[EmbeddedRestServerConfig], classOf[JWTAuthenticationConfig], classOf[TestConfig]), webEnvironment = WebEnvironment.DEFINED_PORT)
class SpectrumCommentRestControllerTest extends AbstractGenericRESTControllerTest[SpectrumComment]("/comments") with Eventually {

  @LocalServerPort
  private val port = 0

  new TestContextManager(this.getClass).prepareTestInstance(this)


  "SpectrumCommentRestControllerTest" must {
    RestAssured.baseURI = s"http://localhost:$port/rest"
  }

  /**
    * object to use for gets
    *
    * @return
    */
  override def getValue: SpectrumComment = SpectrumComment("test", "MoNA000001", "test user", "test@test", "commment")

  /**
    * returns an id for us for testing
    *
    * @return
    */
  override def getId: String = getValue.id

  override val saveRequiresAuthentication: Boolean = false
}
