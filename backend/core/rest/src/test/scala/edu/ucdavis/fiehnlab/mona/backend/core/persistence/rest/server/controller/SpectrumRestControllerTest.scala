package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller

import java.io.InputStreamReader

import com.fasterxml.jackson.databind.ObjectMapper
import com.jayway.restassured.RestAssured
import com.jayway.restassured.RestAssured._
import com.jayway.restassured.config.ObjectMapperConfig
import com.jayway.restassured.mapper.factory.Jackson2ObjectMapperFactory
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.HelperTypes.WrappedString
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.{Spectrum, Splash}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.{JSONDomainReader, MonaMapper}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.ISpectrumMongoRepositoryCustom
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.config.EmbeddedRestServerConfig
import edu.ucdavis.fiehnlab.mona.backend.core.service.persistence.SpectrumPersistenceService
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.boot.test.{SpringApplicationConfiguration, WebIntegrationTest}
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
  * Created by wohlgemuth on 3/1/16.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[StartServerConfig],classOf[EmbeddedRestServerConfig]))
@WebIntegrationTest(Array("server.port=0"))
class SpectrumRestControllerTest extends WordSpec with LazyLogging{

  @Value( """${local.server.port}""")
  val port: Int = 0

  @Autowired
  val spectrumRepository: SpectrumPersistenceService = null

  //required for spring and scala tes
  new TestContextManager(this.getClass()).prepareTestInstance(this)


  "we will be connecting to the REST controller" when {

    RestAssured.config = RestAssured.config().objectMapperConfig(ObjectMapperConfig.objectMapperConfig().jackson2ObjectMapperFactory(new Jackson2ObjectMapperFactory {
      override def create(aClass: Class[_], s: String): ObjectMapper = {
        logger.info("registering rest assured mapper")
        MonaMapper.create
      }
    }))


    RestAssured.baseURI = s"http://localhost:${port}/rest"

    "while working in it" should {


      "we should be able to add spectra using POST at /rest/spectra" in {

        spectrumRepository.deleteAll()

        val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")))

        val countBefore = spectrumRepository.count()

        assert(countBefore == 0)

        for (spectrum <- exampleRecords) {

          logger.debug("starting post request")
          given().contentType("application/json; charset=UTF-8").body(spectrum).when().post("/spectra").then().statusCode(200)
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

      "we should be able to delete a spectra using DELETE at /rest/spectra" in {
        val firstRecords = given().contentType("application/json; charset=UTF-8").when().get("/spectra?size=10").then().statusCode(200).extract().body().as(classOf[Array[Spectrum]])

        val countBefore = spectrumRepository.count()

        for (spec <- firstRecords) {
          given().when().delete(s"/spectra/${spec.id}").then().statusCode(200)
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

        given().contentType("application/json; charset=UTF-8").body(modifiedSpectrum).when().post("/spectra").then().statusCode(200)

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

        val spectrumIdMoved = given().contentType("application/json; charset=UTF-8").when().body(spectrumByID).put(s"/spectra/${spectrum.id}").then().statusCode(200).extract().body().as(classOf[Spectrum])

        given().contentType("application/json; charset=UTF-8").when().get(s"/spectra/${spectrum.id}").then().statusCode(200)

      }


      "we should be able to update a spectrum at a given path using PUT as /rest/spectra " in {

        val spectrum = given().contentType("application/json; charset=UTF-8").when().get("/spectra?size=1").then().statusCode(200).extract().body().as(classOf[Array[Spectrum]]).head

        val spectrumByID = given().contentType("application/json; charset=UTF-8").when().get(s"/spectra/${spectrum.id}").then().statusCode(200).extract().body().as(classOf[Spectrum])

        val spectrumIdMoved = given().contentType("application/json; charset=UTF-8").when().body(spectrumByID).put(s"/spectra/TADA_NEW_ID").then().statusCode(200).extract().body().as(classOf[Spectrum])

        val spectrumByIDNew = given().contentType("application/json; charset=UTF-8").when().get(s"/spectra/TADA_NEW_ID").then().statusCode(200).extract().body().as(classOf[Spectrum])


        //should not exist anymore
        given().contentType("application/json; charset=UTF-8").when().get(s"/spectra/${spectrum.id}").then().statusCode(404)


      }
    }
  }
}
