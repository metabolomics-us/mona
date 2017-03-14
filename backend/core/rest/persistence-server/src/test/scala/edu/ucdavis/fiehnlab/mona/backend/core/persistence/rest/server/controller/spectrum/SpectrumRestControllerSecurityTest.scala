package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.spectrum

import java.io.InputStreamReader

import com.jayway.restassured.RestAssured
import com.jayway.restassured.RestAssured.given
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Spectrum, Submitter}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.{ISubmitterMongoRepository, SequenceMongoRepository}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.config.{EmbeddedRestServerConfig, TestConfig}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.AbstractSpringControllerTest
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rsql.RSQLRepositoryCustom
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.service.persistence.SpectrumPersistenceService
import org.junit.runner.RunWith
import org.scalatest.concurrent.Eventually
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.http.MediaType
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import scala.concurrent.duration._

/**
  * Created by sajjan on 3/13/17.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[EmbeddedRestServerConfig], classOf[JWTAuthenticationConfig], classOf[TestConfig]))
class SpectrumRestControllerSecurityTest extends AbstractSpringControllerTest with Eventually {

  @Autowired
  val spectrumRepository: SpectrumPersistenceService = null

  @Autowired
  val spectrumMongoRepository: PagingAndSortingRepository[Spectrum, String] with RSQLRepositoryCustom[Spectrum, String] = null

  @Autowired
  val spectrumElasticRepository: PagingAndSortingRepository[Spectrum, String] with RSQLRepositoryCustom[Spectrum, String] = null

  @Autowired
  val submitterRepository: ISubmitterMongoRepository = null

  @Autowired
  private val sequenceRepository: SequenceMongoRepository = null

  new TestContextManager(this.getClass).prepareTestInstance(this)


  "spectrum persistence security should be reliable" when {

    RestAssured.baseURI = s"http://localhost:$port/rest"

    val curatedRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/curatedRecords.json")))


    "we should be able to reset the repository" in {
      spectrumRepository.deleteAll()
      spectrumMongoRepository.deleteAll()
      spectrumElasticRepository.deleteAll()
      submitterRepository.deleteAll()
      sequenceRepository.deleteAll()

      eventually(timeout(10 seconds)) {
        assert(spectrumRepository.count() == 0)
        assert(spectrumMongoRepository.count() == 0)
        assert(spectrumElasticRepository.count() == 0)
        assert(submitterRepository.count() == 0)
        assert(sequenceRepository.count() == 0)
      }
    }

    "we expect POST requests" should {

      "fail if no submitter is available" in {
        authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").body(curatedRecords.head).when().post("/spectra").`then`().statusCode(403)
        assert(spectrumRepository.count() == 0)
      }

      "create a test submitter" in {
        submitterRepository.save(Submitter("test", "test", "", "", ""))
        submitterRepository.save(Submitter("test2", "test2", "", "", ""))
        assert(submitterRepository.count() == 2)
      }

      "upload a spectrum as a regular user" in {
        val result: Spectrum = authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").body(curatedRecords.head).when().post("/spectra").`then`().statusCode(200).extract().as(classOf[Spectrum])
        assert(spectrumRepository.count() == 1)
        assert(result.id == curatedRecords.head.id)
        assert(result.submitter.id == "test")
      }

      "overwrite the spectrum as the same user" in {
        authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").body(curatedRecords.head).when().post("/spectra").`then`().statusCode(200)
        assert(spectrumRepository.count() == 1)
      }

      "not overwrite the spectrum as a different regular user" in {
        authenticate("test2", "test-secret").contentType("application/json; charset=UTF-8").body(curatedRecords.head).when().post("/spectra").`then`().statusCode(409)
        assert(spectrumRepository.count() == 1)
      }

      "assign a MoNA id if one is not provided" in {
        val result: Spectrum = authenticate("test2", "test-secret").contentType("application/json; charset=UTF-8").body(curatedRecords.last.copy(id = null)).when().post("/spectra").`then`().statusCode(200).extract().as(classOf[Spectrum])
        assert(result.id.startsWith("MoNA000001"))
        assert(spectrumRepository.count() == 2)
      }
    }

    "we expect PUT requests" should {
      "overwrite the spectrum as the same user" in {
        authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").body(curatedRecords.head).when().put(s"/spectra/${curatedRecords.head.id}").`then`().statusCode(200)
        assert(spectrumRepository.count() == 2)
      }

      "not overwrite the spectrum as a different regular user" in {
        authenticate("test2", "test-secret").contentType("application/json; charset=UTF-8").body(curatedRecords.head).when().put(s"/spectra/${curatedRecords.head.id}").`then`().statusCode(403)
        assert(spectrumRepository.count() == 2)
      }

      "not be able to update user's own spectrum id to overwrite another user's spectrum" in {
        authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").body(curatedRecords.head).when().put(s"/spectra/MoNA000001").`then`().statusCode(409)
        assert(spectrumRepository.count() == 2)
      }

      "update a spectrum id" in {
        given().contentType("application/json; charset=UTF-8").when().get("/spectra/test").`then`().statusCode(404)

        val result: Spectrum = authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").body(curatedRecords.head).when().put(s"/spectra/test").`then`().statusCode(200).extract().as(classOf[Spectrum])
        assert(spectrumRepository.count() == 2)
        assert(result.id == "test")

        given().contentType("application/json; charset=UTF-8").when().get("/spectra/test").`then`().statusCode(200)
      }
    }
  }
}