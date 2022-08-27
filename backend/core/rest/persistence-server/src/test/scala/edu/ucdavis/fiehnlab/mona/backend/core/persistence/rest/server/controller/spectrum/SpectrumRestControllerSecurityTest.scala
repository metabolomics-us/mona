/*
package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.spectrum

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper

import java.io.InputStreamReader
import com.jayway.restassured.RestAssured
import com.jayway.restassured.RestAssured.given
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.{JSONDomainReader, MonaMapper}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.SpectrumResult
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.dao.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.Submitter
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.{SequenceRepository, SubmitterRepository}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.service.SpectrumPersistenceService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.config.{EmbeddedRestServerConfig, TestConfig}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.AbstractSpringControllerTest
import org.scalatest.concurrent.Eventually
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Created by sajjan on 3/13/17.
 * */
@SpringBootTest(classes = Array(classOf[EmbeddedRestServerConfig], classOf[JWTAuthenticationConfig], classOf[TestConfig]), webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Array("test"))
class SpectrumRestControllerSecurityTest extends AbstractSpringControllerTest with Eventually {

  @LocalServerPort
  private val port = 0

  @Autowired
  val spectrumRepository: SpectrumPersistenceService = null

  @Autowired
  val submitterRepository: SubmitterRepository = null

  @Autowired
  private val sequenceRepository: SequenceRepository = null

  @Autowired
  val monaMapper: ObjectMapper = {
    MonaMapper.create
  }

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "spectrum persistence security should be reliable" when {
    RestAssured.baseURI = s"http://localhost:$port/rest"

    val curatedRecords: Array[Spectrum] = monaMapper.readValue(new InputStreamReader(getClass.getResourceAsStream("/curatedRecords.json")), new TypeReference[Array[Spectrum]] {})
    val headRecord: SpectrumResult = new SpectrumResult(curatedRecords.head.getId, curatedRecords.head)
    "we should be able to reset the repository" in {
      spectrumRepository.deleteAll()
      submitterRepository.deleteAll()
      sequenceRepository.deleteAll()

      eventually(timeout(10 seconds)) {
        assert(spectrumRepository.count() == 0)
        assert(submitterRepository.count() == 0)
        assert(sequenceRepository.count() == 0)
      }
    }

    "we expect POST requests" should {

      "fail if no submitter is available" in {
        authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").body(headRecord).when().post("/spectra").`then`().statusCode(403)
        assert(spectrumRepository.count() == 0)
      }

      "create a test submitter" in {
        submitterRepository.save(new Submitter( "test", "", "", ""))
        submitterRepository.save(new Submitter("test2", "", "", ""))
        submitterRepository.save(new Submitter("ntho@chem.uoa.gr", "", "", ""))
        assert(submitterRepository.count() == 3)
      }

      "upload a spectrum as a regular user" in {
        assert(spectrumRepository.count() == 0)

        val result: SpectrumResult = authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").body(headRecord).when().post("/spectra").`then`().statusCode(200).extract().as(classOf[SpectrumResult])
        assert(spectrumRepository.count() == 1)
        assert(result.getMonaId == curatedRecords.head.getId)
        assert(result.getSpectrum.getSubmitter.getEmailAddress == "test")
      }

      "overwrite the spectrum as the same user and ensure that the date is propagated" in {
        val spectrum: SpectrumResult = given().contentType("application/json; charset=UTF-8").when().get("/spectra/AU100601").`then`().statusCode(200).extract().as(classOf[SpectrumResult])
        val result: SpectrumResult = authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").body(headRecord).when().post("/spectra").`then`().statusCode(200).extract().as(classOf[SpectrumResult])
        assert(spectrumRepository.count() == 1)
        assert(spectrum.getSpectrum.getDateCreated.compareTo(result.getSpectrum.getDateCreated) == 0)
      }

      "not overwrite the spectrum as a different regular user" in {
        authenticate("test2", "test-secret").contentType("application/json; charset=UTF-8").body(headRecord).when().post("/spectra").`then`().statusCode(409)
        assert(spectrumRepository.count() == 1)
      }

      "assign a MoNA id if one is not provided" in {
        val copySpectrum = new Spectrum(curatedRecords.head)
        copySpectrum.setId(null)
        val copySpectrumResult = new SpectrumResult(copySpectrum.getId, copySpectrum)
        val result: SpectrumResult = authenticate("test2", "test-secret").contentType("application/json; charset=UTF-8").body(copySpectrumResult).when().post("/spectra").`then`().statusCode(200).extract().as(classOf[SpectrumResult])
        assert(result.getMonaId.startsWith("MoNA_000001"))
        assert(spectrumRepository.count() == 2)
      }
    }

    "we expect PUT requests" should {
      "overwrite the spectrum as the same user" in {
        authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").body(headRecord).when().put(s"/spectra/${curatedRecords.head.getId}").`then`().statusCode(200)
        assert(spectrumRepository.count() == 2)
      }

      "not overwrite the spectrum as a different regular user" in {
        authenticate("test2", "test-secret").contentType("application/json; charset=UTF-8").body(headRecord).when().put(s"/spectra/${curatedRecords.head.getId}").`then`().statusCode(403)
        assert(spectrumRepository.count() == 2)
      }

      "not be able to update user's own spectrum id to overwrite another user's spectrum" in {
        authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").body(headRecord).when().put(s"/spectra/MoNA_000001").`then`().statusCode(409)
        assert(spectrumRepository.count() == 2)
      }

      "update a spectrum id" in {
        given().contentType("application/json; charset=UTF-8").when().get("/spectra/test").`then`().statusCode(404)

        val result: SpectrumResult = authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").body(headRecord).when().put(s"/spectra/test").`then`().statusCode(200).extract().as(classOf[SpectrumResult])
        assert(spectrumRepository.count() == 2)
        assert(result.getMonaId == "test")

        given().contentType("application/json; charset=UTF-8").when().get("/spectra/test").`then`().statusCode(200)
      }
    }
  }
}
*/
