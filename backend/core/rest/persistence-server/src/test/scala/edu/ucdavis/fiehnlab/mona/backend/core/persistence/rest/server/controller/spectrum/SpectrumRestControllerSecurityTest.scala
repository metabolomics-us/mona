package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.spectrum

import java.io.InputStreamReader

import com.jayway.restassured.RestAssured
import com.jayway.restassured.RestAssured.given
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{BlacklistedSplash, Spectrum, Submitter}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.{BlacklistedSplashMongoRepository, ISubmitterMongoRepository, SequenceMongoRepository}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.config.{EmbeddedRestServerConfig, TestConfig}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.AbstractSpringControllerTest
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rsql.RSQLRepositoryCustom
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.service.persistence.SpectrumPersistenceService
import edu.ucdavis.fiehnlab.spectra.hash.core.types.SpectraType
import edu.ucdavis.fiehnlab.spectra.hash.core.util.SplashUtil
import org.junit.runner.RunWith
import org.scalatest.concurrent.Eventually
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.embedded.LocalServerPort
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Created by sajjan on 3/13/17.
  */
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[EmbeddedRestServerConfig], classOf[JWTAuthenticationConfig], classOf[TestConfig]), webEnvironment = WebEnvironment.DEFINED_PORT)
class SpectrumRestControllerSecurityTest extends AbstractSpringControllerTest with Eventually {

  @LocalServerPort
  private val port: String = null

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

  @Autowired
  val blacklistedSplashRepository: BlacklistedSplashMongoRepository = null

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
      blacklistedSplashRepository.deleteAll()

      eventually(timeout(10 seconds)) {
        assert(spectrumRepository.count() == 0)
        assert(spectrumMongoRepository.count() == 0)
        assert(spectrumElasticRepository.count() == 0)
        assert(submitterRepository.count() == 0)
        assert(sequenceRepository.count() == 0)
        assert(blacklistedSplashRepository.count() == 0)
      }
    }

    "add a blacklisted spectrum" in {
      val splash: String = SplashUtil.splash(curatedRecords.last.spectrum, SpectraType.MS)
      blacklistedSplashRepository.save(BlacklistedSplash(splash))
      assert(blacklistedSplashRepository.count() == 1)
    }

    "we expect POST requests" should {

      "fail if no submitter is available" in {
        authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").body(curatedRecords.head).log().all().when().post("/spectra").`then`().statusCode(403)
        assert(spectrumRepository.count() == 0)
      }

      "create a test submitter" in {
        submitterRepository.save(Submitter("test", "test", "", "", ""))
        submitterRepository.save(Submitter("test2", "test2", "", "", ""))
        assert(submitterRepository.count() == 2)
      }

      "upload a spectrum as a regular user" in {
        val result: Spectrum = authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").body(curatedRecords.head).log().all().when().post("/spectra").`then`().statusCode(200).extract().as(classOf[Spectrum])
        assert(spectrumRepository.count() == 1)
        assert(result.id == curatedRecords.head.id)
        assert(result.submitter.id == "test")
      }

      "overwrite the spectrum as the same user" in {
        authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").body(curatedRecords.head).log().all().when().post("/spectra").`then`().statusCode(200)
        assert(spectrumRepository.count() == 1)
      }

      "not overwrite the spectrum as a different regular user" in {
        authenticate("test2", "test-secret").contentType("application/json; charset=UTF-8").body(curatedRecords.head).log().all().when().post("/spectra").`then`().statusCode(409)
        assert(spectrumRepository.count() == 1)
      }

      "assign a MoNA id if one is not provided" in {
        val result: Spectrum = authenticate("test2", "test-secret").contentType("application/json; charset=UTF-8").body(curatedRecords.head.copy(id = null)).log().all().when().post("/spectra").`then`().statusCode(200).extract().as(classOf[Spectrum])
        assert(result.id.startsWith("MoNA000001"))
        assert(spectrumRepository.count() == 2)
      }

      "fail if a blacklisted spectrum is submitted" in {
        authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").body(curatedRecords.last).log().all().when().post(s"/spectra").`then`().statusCode(422)
      }
    }

    "we expect PUT requests" should {
      "overwrite the spectrum as the same user" in {
        authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").body(curatedRecords.head).log().all().when().put(s"/spectra/${curatedRecords.head.id}").`then`().statusCode(200)
        assert(spectrumRepository.count() == 2)
      }

      "not overwrite the spectrum as a different regular user" in {
        authenticate("test2", "test-secret").contentType("application/json; charset=UTF-8").body(curatedRecords.head).log().all().when().put(s"/spectra/${curatedRecords.head.id}").`then`().statusCode(403)
        assert(spectrumRepository.count() == 2)
      }

      "not be able to update user's own spectrum id to overwrite another user's spectrum" in {
        authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").body(curatedRecords.head).log().all().when().put(s"/spectra/MoNA000001").`then`().statusCode(409)
        assert(spectrumRepository.count() == 2)
      }

      "update a spectrum id" in {
        given().contentType("application/json; charset=UTF-8").log().all().when().get("/spectra/test").`then`().statusCode(404)

        val result: Spectrum = authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").body(curatedRecords.head).log().all().when().put(s"/spectra/test").`then`().statusCode(200).extract().as(classOf[Spectrum])
        assert(spectrumRepository.count() == 2)
        assert(result.id == "test")

        given().contentType("application/json; charset=UTF-8").log().all().when().get("/spectra/test").`then`().statusCode(200)
      }

      "fail if a blacklisted spectrum is submitted" in {
        authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").body(curatedRecords.last).log().all().when().put(s"/spectra/${curatedRecords.last.id}").`then`().statusCode(422)
      }
    }
  }
}
