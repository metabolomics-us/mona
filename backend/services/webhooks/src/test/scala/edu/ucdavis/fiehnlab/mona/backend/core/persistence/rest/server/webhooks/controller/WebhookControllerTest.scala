package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.controller

import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.service.MongoLoginService
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.types.TokenSecret
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config.EmbeddedMongoDBConfiguration
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.AbstractGenericRESTControllerTest
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.config.WebHookSecurity
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.types.WebHook
import org.junit.runner.RunWith
import org.springframework.boot.test.{SpringApplicationConfiguration, WebIntegrationTest}
import org.springframework.context.annotation.{Bean, Configuration, Import}
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[WebHookSecurity],classOf[JWTAuthenticationConfig], classOf[TestConfig]))
@WebIntegrationTest(Array("server.port=0"))
class WebhookControllerTest extends AbstractGenericRESTControllerTest[WebHook]("/webhooks") {

  //required for spring and scala tes
  new TestContextManager(this.getClass()).prepareTestInstance(this)

  /**
    * object to use for gets
    *
    * @return
    */
  override def getValue: WebHook = WebHook("my test hook",s"http://localhost:${port}/info","just for testing")

  /**
    * returns an id for us for testing
    *
    * @return
    */
  override def getId: String = getValue.name

}


@Configuration
@Import(Array(classOf[EmbeddedMongoDBConfiguration]))
class TestConfig {

  /**
    * the service which actually does the login for us
    *
    * @return
    */
  @Bean
  def loginServiceDelegate:LoginService = new MongoLoginService


  /**
    * the token secret used during the testing phase
    *
    * @return
    */
  @Bean
  def tokenSecret: TokenSecret = TokenSecret("sadaskdkljsalkd")

}
