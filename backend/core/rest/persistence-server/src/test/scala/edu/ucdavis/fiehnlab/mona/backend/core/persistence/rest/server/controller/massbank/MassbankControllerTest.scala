/*
package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.massbank

import com.jayway.restassured.RestAssured
import com.jayway.restassured.RestAssured._
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.config.{EmbeddedRestServerConfig, TestConfig}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.AbstractSpringControllerTest
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rsql.RSQLRepositoryCustom
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.service.persistence.SpectrumPersistenceService
import org.junit.runner.RunWith
import org.scalatest.concurrent.Eventually
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner

import scala.io.Source

/**
  * Created by wohlgemuth on 5/12/16.
  */
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[EmbeddedRestServerConfig], classOf[JWTAuthenticationConfig], classOf[TestConfig]), webEnvironment = WebEnvironment.DEFINED_PORT)
class MassbankControllerTest extends AbstractSpringControllerTest with Eventually with LazyLogging {

  @LocalServerPort
  private val port = 0

  @Autowired
  val spectrumRepository: SpectrumPersistenceService = null

  @Autowired
  val spectrumMongoRepository: PagingAndSortingRepository[Spectrum, String] with RSQLRepositoryCustom[Spectrum, String] = null

  @Autowired
  val spectrumElasticRepository: PagingAndSortingRepository[Spectrum, String] with RSQLRepositoryCustom[Spectrum, String] = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "MassbankControllerTest" must {
    RestAssured.baseURI = s"http://localhost:$port/rest"

    val src: Source = Source.fromURL(getClass.getResource(s"/massbank/singleRecord.txt"))

    "submit" should {
      val strings = src.mkString

      "require authorization" in {
        given().contentType("application/json; charset=UTF-8").body(strings).log().all(true).when().post("/upload/massbank").`then`().statusCode(401)
      }

      "upload a spectra, with authorization" in {
        val result: Spectrum = authenticate().contentType("application/json; charset=UTF-8").body(strings).log().all(true).when().post("/upload/massbank").`then`().log().all(true).statusCode(200).extract().as(classOf[Spectrum])

        assert(result.id == "PR100162")
        assert(result.compound.length == 1)
      }
    }
  }
}
*/
