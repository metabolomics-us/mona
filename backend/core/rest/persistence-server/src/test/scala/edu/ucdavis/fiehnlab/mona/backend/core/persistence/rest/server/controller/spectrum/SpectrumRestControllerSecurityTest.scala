package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.spectrum

import java.io.InputStreamReader
import com.jayway.restassured.RestAssured
import com.jayway.restassured.RestAssured.given
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{DeletionJob, Spectrum, Submitter}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.SubmitterRepository
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.service.SpectrumPersistenceService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.config.{EmbeddedRestServerConfig, TestConfig}
import org.scalatest.concurrent.Eventually
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.AbstractSpringControllerTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

import scala.concurrent.duration._
import scala.jdk.CollectionConverters._
import scala.language.postfixOps

/**
  * Created by sajjan on 3/13/17.
 * */
@SpringBootTest(classes = Array(classOf[EmbeddedRestServerConfig], classOf[JWTAuthenticationConfig], classOf[TestConfig]), webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Array("test", "mona.persistence", "mona.persistence.init"))
class SpectrumRestControllerSecurityTest extends AbstractSpringControllerTest with Eventually {

  @LocalServerPort
  private val port = 0

  @Autowired
  val spectrumRepository: SpectrumPersistenceService = null

  @Autowired
  val submitterRepository: SubmitterRepository = null

  @Autowired
  val spectrumPersistenceService: SpectrumPersistenceService = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "spectrum persistence security should be reliable" when {
    RestAssured.baseURI = s"http://localhost:$port/rest"

    val curatedRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/curatedRecords.json")))
    val headRecord: Spectrum = curatedRecords.head
    "we should be able to reset the repository" in {
      spectrumRepository.deleteAll()
      submitterRepository.deleteAll()

      eventually(timeout(10 seconds)) {
        assert(spectrumRepository.count() == 0)
        assert(submitterRepository.count() == 0)
      }
    }

    "we expect POST requests" should {

      "fail if no submitter is available" in {
        authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").body(headRecord).when().post("/spectra").`then`().statusCode(403)
        assert(spectrumRepository.count() == 0)
      }

      "create a test submitter" in {
        submitterRepository.save(new Submitter( "test", "test", "test", "test"))
        submitterRepository.save(new Submitter("test2", "", "", ""))
        submitterRepository.save(new Submitter("ntho@chem.uoa.gr", "", "", ""))
        assert(submitterRepository.count() == 3)
      }

      "upload a spectrum as a regular user" in {
        assert(spectrumRepository.count() == 0)

        val result: Spectrum = authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").body(headRecord).when().post("/spectra").`then`().statusCode(200).extract().as(classOf[Spectrum])
        assert(spectrumRepository.count() == 1)
        assert(result.getId == curatedRecords.head.getId)
        assert(result.getSubmitter.getEmailAddress == "test")
      }

      "overwrite the spectrum as the same user and ensure that the date is propagated" in {
        val spectrum: Spectrum = given().contentType("application/json; charset=UTF-8").when().get("/spectra/AU100601").`then`().statusCode(200).extract().as(classOf[Spectrum])
        val result: Spectrum = authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").body(headRecord).when().post("/spectra").`then`().statusCode(200).extract().as(classOf[Spectrum])
        assert(spectrumPersistenceService.count() == 1)
        assert(spectrum.getDateCreated.compareTo(result.getDateCreated) == 0)

      }

      "not overwrite the spectrum as a different regular user" in {
        authenticate("test2", "test-secret").contentType("application/json; charset=UTF-8").body(headRecord).when().post("/spectra").`then`().statusCode(409)
        assert(spectrumRepository.count() == 1)
      }

      "assign a MoNA id if one is not provided" in {
        val copySpectrum = new Spectrum(curatedRecords.head)
        copySpectrum.setId(null)
        val result: Spectrum = authenticate("test2", "test-secret").contentType("application/json; charset=UTF-8").body(copySpectrum).when().post("/spectra").`then`().statusCode(200).extract().as(classOf[Spectrum])
        assert(result.getId.startsWith("MoNA_0000001"))
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
        authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").body(headRecord).when().put(s"/spectra/MoNA_0000001").`then`().statusCode(409)
        assert(spectrumRepository.count() == 2)
      }

      "update a spectrum id" in {
        given().contentType("application/json; charset=UTF-8").when().get("/spectra/test").`then`().statusCode(404)

        val result: Spectrum = authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").body(headRecord).when().put(s"/spectra/test").`then`().statusCode(200).extract().as(classOf[Spectrum])
        assert(spectrumRepository.count() == 2)
        assert(result.getId == "test")

        given().contentType("application/json; charset=UTF-8").when().get("/spectra/test").`then`().statusCode(200)
      }
    }

    "we expect DELETE requests" should {
      "reject deletion of a spectrum the user does not own" in {
        authenticate("test2", "test-secret").contentType("application/json; charset=UTF-8").when().delete("/spectra/test").`then`().statusCode(403)
        assert(spectrumRepository.count() == 2)
      }

      "allow the owner to delete their own spectrum" in {
        authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").when().delete("/spectra/test").`then`().statusCode(200)
        assert(spectrumRepository.count() == 1)
      }
    }

    "we expect bulk DELETE requests to be admin only" should {
      "reject a query based deletion for a non admin user" in {
        authenticate("test", "test-secret").contentType("application/json; charset=UTF-8")
          .queryParam("query", "tags.text:'LCMS'")
          .when().delete("/spectra/search").`then`().statusCode(403)
      }

      "reject an id based deletion for a non admin user" in {
        authenticate("test2", "test-secret").contentType("application/json; charset=UTF-8")
          .body(List("MoNA_0000001").asJava)
          .when().delete("/spectra").`then`().statusCode(403)
      }

      "reject an empty query deletion even for an admin since it would match everything" in {
        authenticate().contentType("application/json; charset=UTF-8")
          .when().delete("/spectra/search").`then`().statusCode(400)
      }

      "accept an admin query based deletion and return a scheduled job retrievable by status" in {
        val job = authenticate().contentType("application/json; charset=UTF-8")
          .queryParam("query", "tags.text:'NONEXISTENT_LIBRARY_XYZ'")
          .when().delete("/spectra/search").`then`().statusCode(202).extract().as(classOf[DeletionJob])

        assert(job.getId != null)
        assert(job.getStatus == DeletionJob.STATUS_SCHEDULED)

        // the status endpoint is open and returns the tracking row
        given().contentType("application/json; charset=UTF-8")
          .when().get(s"/spectra/delete/status/${job.getId}").`then`().statusCode(200)
      }

      "return 404 from the status endpoint for an unknown job" in {
        given().contentType("application/json; charset=UTF-8")
          .when().get("/spectra/delete/status/does-not-exist").`then`().statusCode(404)
      }

      "actually delete the spectra through the background job for an admin" in {
        val ids = spectrumPersistenceService.findAll(PageRequest.of(0, 100)).getContent.asScala.map(_.getId).toList
        assert(ids.nonEmpty)

        authenticate().contentType("application/json; charset=UTF-8")
          .body(ids.asJava)
          .when().delete("/spectra").`then`().statusCode(202)

        // the listener consumes the durable queue and deletes asynchronously
        eventually(timeout(30 seconds)) {
          assert(spectrumPersistenceService.count() == 0)
        }
      }
    }

    /**
      * These depend on /error being excluded from the security chain in RestServerConfig. Spring
      * re-dispatches an error to /error and Spring Boot runs the chain on the ERROR dispatch as well,
      * where JWTAuthenticationFilter answers 401 for a request without an authorization header. Since
      * the open GET endpoints never carry one, every status below used to reach the caller as an empty
      * 401 instead of its own
      */
    "errors raised on the openly accessible endpoints" should {

      "keep a bad request status rather than report an authentication failure" in {
        given().contentType("application/json; charset=UTF-8")
          .queryParam("size", "abc")
          .when().get("/spectra").`then`().statusCode(400)
      }

      "keep a not found status for an unmapped path below an open endpoint" in {
        given().contentType("application/json; charset=UTF-8")
          .when().get("/spectra/a/b/c").`then`().statusCode(404)
      }
    }
  }
}
