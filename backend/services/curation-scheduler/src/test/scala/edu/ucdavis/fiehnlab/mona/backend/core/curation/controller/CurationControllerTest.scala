package edu.ucdavis.fiehnlab.mona.backend.core.curation.controller

import java.io.InputStreamReader

import com.jayway.restassured.RestAssured
import com.jayway.restassured.RestAssured._
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.service.MongoLoginService
import edu.ucdavis.fiehnlab.mona.backend.core.curation.CurrationScheduler
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.ISpectrumMongoRepositoryCustom
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.AbstractSpringControllerTest
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
  * Created by wohlg on 4/13/2016.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[CurrationScheduler]))
class CurationControllerTest extends AbstractSpringControllerTest {

  @Autowired
  val mongoRepository: ISpectrumMongoRepositoryCustom = null

  new TestContextManager(this.getClass()).prepareTestInstance(this)

  "CurationControllerTest" should {

    RestAssured.baseURI = s"http://localhost:${port}/rest"
    val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")))

    "load some data" in {

      mongoRepository.deleteAll()
      for (spectrum <- exampleRecords) {
        mongoRepository.save(spectrum)
      }
    }

    "these must all fail, since we require to be logged in " must {
      "curateByQuery" in {
        given().contentType("application/json; charset=UTF-8").when().get("/curation?query=metaData=q='name==\"ion mode\" and value==negative'").then().statusCode(401)
      }

      "curateById" in {
        given().contentType("application/json; charset=UTF-8").when().get("/curation/252").then().statusCode(401)
      }

      "curateAll" in {
        given().contentType("application/json; charset=UTF-8").when().get("/curation").then().statusCode(401)
      }

    }


    "these must all fail, since we require to be an admin " must {
      "curateByQuery" in {
        authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").when().get("/curation?query=metaData=q='name==\"ion mode\" and value==negative'").then().statusCode(403)
      }

      "curateById" in {
        authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").when().get("/curation/252").then().statusCode(403)
      }

      "curateAll" in {
        authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").when().get("/curation").then().statusCode(403)
      }

    }

    "these must all pass, since we are logged in " must {
      "curateByQuery" in {
        val result = authenticate().contentType("application/json; charset=UTF-8").when().get("/curation?query=metaData=q='name==\"ion mode\" and value==negative'").then().statusCode(200).extract().body().as(classOf[CurationJobScheduled])
        assert(result.count == 25)
      }

      "curateByIdWithWrongIdShouldReturn404" in {
        authenticate().contentType("application/json; charset=UTF-8").when().get("/curation/IDONOTEXIST").then().statusCode(404)
      }

      "curateById" in {
        val spec: Spectrum = mongoRepository.findAll().iterator().next()
        val result:CurationJobScheduled = authenticate().contentType("application/json; charset=UTF-8").when().get(s"/curation/${spec.id}").then().statusCode(200).extract().body().as(classOf[CurationJobScheduled])

        assert(result.count == 1)
      }


      "curateAll" in {
        val result:CurationJobScheduled = authenticate().contentType("application/json; charset=UTF-8").when().get("/curation").then().statusCode(200).extract().body().as(classOf[CurationJobScheduled])
        assert(result.count == exampleRecords.length)
      }

    }
  }
}

@Configuration
class TestConfig {

  /**
    * the service which actually does the login for us
    *
    * @return
    */
  @Bean
  def loginService: LoginService = new MongoLoginService
}