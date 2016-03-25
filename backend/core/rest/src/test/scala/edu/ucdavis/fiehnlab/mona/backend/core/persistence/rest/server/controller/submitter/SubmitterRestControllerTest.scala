package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.submitter

import java.io.InputStreamReader

import com.fasterxml.jackson.databind.ObjectMapper
import com.jayway.restassured.RestAssured
import com.jayway.restassured.RestAssured._
import com.jayway.restassured.config.ObjectMapperConfig
import com.jayway.restassured.mapper.factory.Jackson2ObjectMapperFactory
import com.jayway.restassured.specification.RequestSpecification
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Spectrum, Submitter}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.{JSONDomainReader, MonaMapper}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.ISubmitterMongoRepository
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.config.EmbeddedRestServerConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.{AbstractGenericRESTControllerTest, StartServerConfig}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.security.config.JWTRestSecurityConfig
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.boot.test.{SpringApplicationConfiguration, WebIntegrationTest}
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
  * Created by sajjan on 3/9/16.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[StartServerConfig], classOf[EmbeddedRestServerConfig], classOf[JWTRestSecurityConfig]))
@WebIntegrationTest(Array("server.port=0"))
class SubmitterRestControllerTest extends AbstractGenericRESTControllerTest[Submitter]("/submitters") {

  @Autowired
  val submitterRepository: ISubmitterMongoRepository = null

  //required for spring and scala tes
  new TestContextManager(this.getClass()).prepareTestInstance(this)

  /**
    * object to use for gets
    *
    * @return
    */
  override def getValue: Submitter = JSONDomainReader.create[Spectrum].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecord.json"))).submitter

  /**
    * returns an id for us for testing
    *
    * @return
    */
  override def getId: String = getValue.id

}
