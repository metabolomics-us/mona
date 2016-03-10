package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.controller

import java.io.{File, FileReader}

import com.fasterxml.jackson.databind.ObjectMapper
import com.jayway.restassured.RestAssured
import com.jayway.restassured.RestAssured._
import com.jayway.restassured.config.ObjectMapperConfig
import com.jayway.restassured.mapper.factory.Jackson2ObjectMapperFactory
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.{Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.{JSONDomainReader, MonaMapper}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.{ISubmitterMongoRepository}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.Application
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.boot.test.{IntegrationTest, SpringApplicationConfiguration}
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.web.WebAppConfiguration

/**
  * Created by sajjan on 3/9/16.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[Application]))
@WebAppConfiguration
@IntegrationTest(Array("server.port:0"))
class SubmitterRestControllerTest extends WordSpec {

  @Value( """${local.server.port}""")
  val port: Int = 0


  @Autowired
  val submitterRepository: ISubmitterMongoRepository = null

  //required for spring and scala tes
  new TestContextManager(this.getClass()).prepareTestInstance(this)


  "we will be connecting to the REST controller" when {

    RestAssured.config = RestAssured.config().objectMapperConfig(ObjectMapperConfig.objectMapperConfig().jackson2ObjectMapperFactory(new Jackson2ObjectMapperFactory {
      override def create(aClass: Class[_], s: String): ObjectMapper = MonaMapper.create
    }))


    RestAssured.baseURI = s"http://localhost:${port}/rest"

    "while working in it" should {

      "we should be able to add submitter using POST at /rest/submitters" in {

        submitterRepository.deleteAll()

        val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new FileReader(new File("src/test/resources/monaRecords.json")))

        assert(submitterRepository.count() == 0)

        for (spectrum <- exampleRecords) {
          given().contentType("application/json; charset=UTF-8").body(spectrum.submitter).when().post("/submitters").then().statusCode(200)
        }

        assert(submitterRepository.count() == exampleRecords.length)
      }

      "we should be able to query all the submitter using GET at /rest/submitters" in {
        val exampleRecords = given().contentType("application/json; charset=UTF-8").when().get("/submitters").then().statusCode(200).extract().body().as(classOf[Array[Spectrum]])
        assert(submitterRepository.count() == exampleRecords.length)
      }
    }
  }
}
