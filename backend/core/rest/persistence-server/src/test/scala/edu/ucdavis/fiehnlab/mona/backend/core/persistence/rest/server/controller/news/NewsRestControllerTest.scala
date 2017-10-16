package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.news

import java.util.Date

import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.domain.NewsEntry
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.config.{EmbeddedRestServerConfig, TestConfig}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.{AbstractGenericRESTControllerTest, AbstractSpringControllerTest}
import org.junit.runner.RunWith
import org.scalatest.concurrent.Eventually
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
  * Created by sajjan on 4/6/17.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[EmbeddedRestServerConfig], classOf[JWTAuthenticationConfig], classOf[TestConfig]))
class NewsRestControllerTest extends AbstractGenericRESTControllerTest[NewsEntry]("/news") with Eventually {

  new TestContextManager(this.getClass).prepareTestInstance(this)

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
