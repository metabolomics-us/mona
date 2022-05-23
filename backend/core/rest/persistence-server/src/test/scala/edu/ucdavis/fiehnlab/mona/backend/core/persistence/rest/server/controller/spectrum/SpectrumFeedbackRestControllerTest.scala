package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.spectrum

import com.jayway.restassured.RestAssured
import com.jayway.restassured.RestAssured.given
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.domain.SpectrumFeedback
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.SpectrumFeedbackMongoRepository
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.config.{EmbeddedRestServerConfig, TestConfig}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.AbstractGenericRESTControllerTest
import org.junit.runner.RunWith
import org.scalatest.concurrent.Eventually
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner

/**
  * Created by sajjan on 4/6/17.
  */
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[EmbeddedRestServerConfig], classOf[JWTAuthenticationConfig], classOf[TestConfig]), webEnvironment = WebEnvironment.DEFINED_PORT)
class SpectrumFeedbackRestControllerTest extends AbstractGenericRESTControllerTest[SpectrumFeedback]("/feedback") with Eventually {

  @LocalServerPort
  private val port = 0

  @Autowired
  val spectrumFeedbackMongoRepository: SpectrumFeedbackMongoRepository = null

  new TestContextManager(this.getClass).prepareTestInstance(this)


  "SpectrumFeedbackRestControllerTest" must {
    RestAssured.baseURI = s"http://localhost:$port/rest"
  }

  /**
    * object to use for gets
    *
    * @return
    */
  override def getValue: SpectrumFeedback = SpectrumFeedback("test", "MoNA000001", "test user", "comment", "commment value")

  /**
    * returns an id for us for testing
    *
    * @return
    */
  override def getId: String = getValue.id

  override val saveRequiresAuthentication: Boolean = false

  override val deleteRequiresAuthentication: Boolean = false
}
