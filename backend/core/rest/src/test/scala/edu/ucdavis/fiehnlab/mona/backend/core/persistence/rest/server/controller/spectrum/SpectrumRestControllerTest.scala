package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.spectrum

import java.io.InputStreamReader

import com.jayway.restassured.RestAssured._
import com.jayway.restassured.specification.RequestSpecification
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Spectrum, Splash}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.config.{BasicRestSecurityConfig, EmbeddedRestServerConfig, JWTRestSecurityConfig}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.{AbstractGenericRESTControllerTest, StartServerConfig}
import edu.ucdavis.fiehnlab.mona.backend.core.service.persistence.SpectrumPersistenceService
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.{SpringApplicationConfiguration, WebIntegrationTest}
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.annotation.DirtiesContext.ClassMode
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
/**
  * Created by wohlgemuth on 3/1/16.
  */
abstract class AbstractSpectrumRestControllerTest extends AbstractGenericRESTControllerTest[Spectrum]("/spectra"){


  /**
    * object to use for gets
    *
    * @return
    */
  override def getValue: Spectrum = JSONDomainReader.create[Spectrum].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecord.json")))

  /**
    * returns an id for us for testing
    *
    * @return
    */
  override def getId: String = getValue.id

  @Autowired
  val spectrumRepository: SpectrumPersistenceService = null


  //required for spring and scala tes
  new TestContextManager(this.getClass()).prepareTestInstance(this)


  "we will be connecting to the REST controller" when {

    "while working in it" should {

      "we should be able to add spectra using POST at /rest/spectra with authentication" in {

        spectrumRepository.deleteAll()

        assert(spectrumRepository.count() == 0)

        val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")))

        val countBefore = spectrumRepository.count()

        assert(countBefore == 0)

        for (spectrum <- exampleRecords) {

          logger.debug("starting post request")
          authenticate().contentType("application/json; charset=UTF-8").body(spectrum).when().post("/spectra").then().statusCode(200)
        }

        val countAfter = spectrumRepository.count()

        assert(countAfter - exampleRecords.length == countBefore)
      }

      "we should be able to query all the spectra using GET at /rest/spectra" in {

        val exampleRecords = given().contentType("application/json; charset=UTF-8").when().get("/spectra").then().statusCode(200).extract().body().as(classOf[Array[Spectrum]])

        assert(spectrumRepository.count() == exampleRecords.length)

      }

      "we should be able to test our pagination, while using GET at /rest/spectra?size=10 to 10 records" in {
        val exampleRecords = given().contentType("application/json; charset=UTF-8").when().get("/spectra?size=10").then().statusCode(200).extract().body().as(classOf[Array[Spectrum]])
        assert(10 == exampleRecords.length)
      }

      "we should be able to test our pagination, while using GET at /rest/spectra?size=10&page=1 to 10 records" in {
        val firstRecords = given().contentType("application/json; charset=UTF-8").when().get("/spectra?size=10").then().statusCode(200).extract().body().as(classOf[Array[Spectrum]])
        assert(10 == firstRecords.length)

        val exampleRecords = given().contentType("application/json; charset=UTF-8").when().get("/spectra?size=10&page=1").then().statusCode(200).extract().body().as(classOf[Array[Spectrum]])
        assert(10 == exampleRecords.length)

        for (spec <- exampleRecords) {
          assert(firstRecords.contains(spec) == false)
        }
      }

      "we need to be authenticated to delete spectra " in {
        given().when().delete(s"/spectra/111").then().statusCode(401)
      }

      "we need to be an admin to delete spectra " in {
        authenticate("test", "test-secret").when().delete(s"/spectra/111").then().statusCode(403)
      }


      "we should be able to delete a spectra using DELETE at /rest/spectra" in {
        val firstRecords = given().contentType("application/json; charset=UTF-8").when().get("/spectra?size=10").then().statusCode(200).extract().body().as(classOf[Array[Spectrum]])

        val countBefore = spectrumRepository.count()

        for (spec <- firstRecords) {
          authenticate().when().delete(s"/spectra/${spec.id}").then().statusCode(200)
        }

        val countAfter = spectrumRepository.count()

        assert(countBefore - countAfter == 10)
      }

      "we should be able to execute custom queries at /rest/spectra/search using GET" in {
        val exampleRecords = given().contentType("application/json; charset=UTF-8").when().get("/spectra/search?query=biologicalCompound.names.name=='META-HYDROXYBENZOIC ACID'").then().statusCode(200).extract().body().as(classOf[Array[Spectrum]])

        assert(exampleRecords.length == 1)
      }

      "we should be able to update a spectra with new properties" in {
        val spectrum = given().contentType("application/json; charset=UTF-8").when().get("/spectra?size=1").then().statusCode(200).extract().body().as(classOf[Array[Spectrum]]).head

        val splash: Splash = spectrum.splash.copy(splash = "tada")
        val modifiedSpectrum: Spectrum = spectrum.copy(splash = splash)
        val countBefore = spectrumRepository.count()

        authenticate().contentType("application/json; charset=UTF-8").body(modifiedSpectrum).when().post("/spectra").then().statusCode(200)

        val countAfter = spectrumRepository.count()
        val spectrumAfterUpdate = given().contentType("application/json; charset=UTF-8").when().get(s"/spectra/${modifiedSpectrum.id}").then().statusCode(200).extract().body().as(classOf[Spectrum])

        assert(spectrumAfterUpdate.splash.splash == modifiedSpectrum.splash.splash)
        assert(countBefore == countAfter)
      }

      "we should be able to receive a spectra by it's ID using GET at /rest/spectra/{id}" in {

        val spectrum = given().contentType("application/json; charset=UTF-8").when().get("/spectra?size=1").then().statusCode(200).extract().body().as(classOf[Array[Spectrum]]).head

        val spectrumByID = given().contentType("application/json; charset=UTF-8").when().get(s"/spectra/${spectrum.id}").then().statusCode(200).extract().body().as(classOf[Spectrum])

        assert(spectrum.id.equals(spectrumByID.id))

      }

      "if a spectra doesn't exist at /rest/spectra/{id}, we should receive a 404 " in {
        given().contentType("application/json; charset=UTF-8").when().get(s"/spectra/TADA1234").then().statusCode(404)
      }

      "we should be able to move a spectrum from one id to another using PUT as /rest/spectra " in {

        val spectrum = given().contentType("application/json; charset=UTF-8").when().get("/spectra?size=1").then().statusCode(200).extract().body().as(classOf[Array[Spectrum]]).head

        val spectrumByID = given().contentType("application/json; charset=UTF-8").when().get(s"/spectra/${spectrum.id}").then().statusCode(200).extract().body().as(classOf[Spectrum])

        val spectrumIdMoved = authenticate().contentType("application/json; charset=UTF-8").when().body(spectrumByID).put(s"/spectra/${spectrum.id}").then().statusCode(200).extract().body().as(classOf[Spectrum])

        given().contentType("application/json; charset=UTF-8").when().get(s"/spectra/${spectrum.id}").then().statusCode(200)

      }

      "we need to be authentificated for PUT requestes" in {
        given().contentType("application/json; charset=UTF-8").when().body(Spectrum).put(s"/spectra/TADA_NEW_ID").then().statusCode(401).extract().body().as(classOf[Spectrum])
      }

      "we should be able to update a spectrum at a given path using PUT as /rest/spectra " in {

        val spectrum = given().contentType("application/json; charset=UTF-8").when().get("/spectra?size=1").then().statusCode(200).extract().body().as(classOf[Array[Spectrum]]).head

        val spectrumByID = given().contentType("application/json; charset=UTF-8").when().get(s"/spectra/${spectrum.id}").then().statusCode(200).extract().body().as(classOf[Spectrum])

        val spectrumIdMoved = authenticate().contentType("application/json; charset=UTF-8").when().body(spectrumByID).put(s"/spectra/TADA_NEW_ID").then().statusCode(200).extract().body().as(classOf[Spectrum])

        val spectrumByIDNew = given().contentType("application/json; charset=UTF-8").when().get(s"/spectra/TADA_NEW_ID").then().statusCode(200).extract().body().as(classOf[Spectrum])


        //should not exist anymore
        given().contentType("application/json; charset=UTF-8").when().get(s"/spectra/${spectrum.id}").then().statusCode(404)


      }
    }
  }

}

/**
  * tests basic authentification
  */
@SpringApplicationConfiguration(classes = Array(classOf[EmbeddedRestServerConfig], classOf[BasicRestSecurityConfig]))
@WebIntegrationTest(Array("server.port=0"))
@RunWith(classOf[SpringJUnit4ClassRunner])
class BasicAuthSpectrumRestControllerTest extends AbstractSpectrumRestControllerTest {

  //required for spring and scala tes
 testContextManager.prepareTestInstance(this)

  override def authenticate(user: String, password: String): RequestSpecification = {
    given().auth().basic(user, password)
  }
}

/**
  * tests basic authentification
  */
@SpringApplicationConfiguration(classes = Array(classOf[EmbeddedRestServerConfig], classOf[JWTRestSecurityConfig]))
@WebIntegrationTest(Array("server.port=0"))
@RunWith(classOf[SpringJUnit4ClassRunner])
class TokenAuthSpectrumRestControllerTest extends AbstractSpectrumRestControllerTest {

  //required for spring and scala tes
  testContextManager.prepareTestInstance(this)

}

