package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.spectrum

import com.jayway.restassured.RestAssured
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.domain.SpectrumFeedback
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.SpectrumFeedbackRepository
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.AbstractGenericRESTControllerTest
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.config.{EmbeddedRestServerConfig, TestConfig}
import org.scalatest.concurrent.Eventually
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

/**
  * Created by sajjan on 4/6/17.
  **/
@ActiveProfiles(Array("test"))
@SpringBootTest(classes = Array(classOf[EmbeddedRestServerConfig], classOf[JWTAuthenticationConfig], classOf[TestConfig]), webEnvironment = WebEnvironment.RANDOM_PORT)
class SpectrumFeedbackRestControllerTest extends AbstractGenericRESTControllerTest[SpectrumFeedback, Long]("/feedback") with Eventually {

  @LocalServerPort
  private val port = 0

  @Autowired
  val spectrumFeedbackMongoRepository: SpectrumFeedbackRepository = null

  new TestContextManager(this.getClass).prepareTestInstance(this)


  "SpectrumFeedbackRestControllerTest" must {
    RestAssured.baseURI = s"http://localhost:$port/rest"
  }

  /**
    * object to use for gets
    *
    * @return
    */
  override def getValue: SpectrumFeedback = new SpectrumFeedback("MoNA000001",  "test user", "comment", "commment value")

  /**
    * returns an id for us for testing
    *
    * @return
    */
  override def getId: Long = getValue.getId

  override val saveRequiresAuthentication: Boolean = false

  override val deleteRequiresAuthentication: Boolean = false
}
