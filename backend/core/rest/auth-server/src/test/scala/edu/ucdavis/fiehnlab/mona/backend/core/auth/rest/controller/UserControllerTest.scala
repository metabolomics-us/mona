package edu.ucdavis.fiehnlab.mona.backend.core.auth.rest.controller

import java.io.InputStreamReader
import java.util.Collections

import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.repository.UserRepository
import edu.ucdavis.fiehnlab.mona.backend.core.auth.rest.config.AuthSecurityConfig
import edu.ucdavis.fiehnlab.mona.backend.core.auth.types.{Role, User}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Spectrum, Submitter}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.ISubmitterMongoRepository
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.AbstractGenericRESTControllerTest
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.{SpringApplicationConfiguration, WebIntegrationTest}
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import scala.collection.JavaConverters._

/**
  * Created by wohlgemuth on 4/4/16.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[JWTAuthenticationConfig], classOf[TestConfig],classOf[AuthSecurityConfig]))
class UserControllerTest extends AbstractGenericRESTControllerTest[User]("/users") {

  //required for spring and scala tes
  new TestContextManager(this.getClass()).prepareTestInstance(this)

  /**
    * object to use for gets
    *
    * @return
    */
  override def getValue: User = User("admi312312n", "a13123dmin", List[Role](Role("ADMIN")).asJava)

  /**
    * returns an id for us for testing
    *
    * @return
    */
  override def getId: String = getValue.username

  override val requiresAuthForAllRequestes: Boolean = true
}
