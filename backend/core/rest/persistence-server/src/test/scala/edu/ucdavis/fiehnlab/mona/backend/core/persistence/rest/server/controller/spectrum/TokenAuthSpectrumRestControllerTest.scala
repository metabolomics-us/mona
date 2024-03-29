package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.spectrum

import java.io.{InputStreamReader, StringWriter}
import com.jayway.restassured.RestAssured
import com.jayway.restassured.RestAssured._
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.{JSONDomainReader, MonaMapper}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.config.{EmbeddedRestServerConfig, TestConfig}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.service.SpectrumPersistenceService
import org.scalatest.concurrent.Eventually
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.http.MediaType
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.ActiveProfiles
import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.AbstractGenericRESTControllerTest
import org.hibernate.Hibernate
import org.scalatest.BeforeAndAfterEach
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Properties
import java.util.Collections


/**
  * Created by wohlgemuth on 3/1/16.
  */
@SpringBootTest(classes = Array(classOf[EmbeddedRestServerConfig], classOf[JWTAuthenticationConfig], classOf[TestConfig]), webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Array("test", "mona.persistence", "mona.persistence.init"))
class TokenAuthSpectrumRestControllerTest extends AbstractGenericRESTControllerTest[Spectrum, String]("/spectra") with Eventually with BeforeAndAfterEach{

  @LocalServerPort
  private val port: String = null

  val keepRunning: Boolean = Properties.envOrElse("keep.server.running", "false").toBoolean

  @Autowired
  val spectrumRepository: SpectrumPersistenceService = null

  @Autowired
  val monaMapper: ObjectMapper = {
    MonaMapper.create
  }

  @Autowired
  private val transactionManager: PlatformTransactionManager = null

  private var transactionTemplate: TransactionTemplate = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  protected override def beforeEach() = (
    transactionTemplate = new TransactionTemplate(transactionManager)
    )

  protected override def afterEach() = (
    transactionTemplate = null
    )

  new TestContextManager(this.getClass).prepareTestInstance(this)

  override val deleteRequiresAuthentication: Boolean = false

  "we will be connecting to the REST controller" when {
    RestAssured.baseURI = s"http://localhost:$port/rest"

    "while working in it" should {
      val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")))
      val curatedRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/curatedRecords.json")))

      "have a working validation system" must {
        val mapper = MonaMapper.create

        "works on a correct spectra upload" in {
          val copySpectrum = new Spectrum(curatedRecords.head)

          val writer = new StringWriter()
          mapper.writeValue(writer, copySpectrum)

          val content = writer.toString
          logger.info(s"content: $content")

          authenticate().contentType("application/json; charset=UTF-8").body(content).when().post("/spectra").`then`().statusCode(200)
        }

        "fails when an empty spectra is uploaded" in {
          val copySpectrum = new Spectrum(curatedRecords.head)
          copySpectrum.setSpectrum(null)

          val writer = new StringWriter()
          mapper.writeValue(writer, copySpectrum)

          val content = writer.toString
          logger.info(s"${content}")
          authenticate().contentType("application/json; charset=UTF-8").body(content).when().post("/spectra").`then`().statusCode(400)
        }

        "fails if no submitter is provided" in {
          val copySpectrum = new Spectrum(curatedRecords.head)
          copySpectrum.setSubmitter(null)

          val writer = new StringWriter()
          mapper.writeValue(writer, copySpectrum)

          val content = writer.toString
          authenticate().contentType("application/json; charset=UTF-8").body(content).when().post("/spectra").`then`().statusCode(400)
        }

        "fails if no compound is provided" in {
          val copySpectrum = new Spectrum(curatedRecords.head)
          copySpectrum.setCompound(Collections.emptyList())

          val writer = new StringWriter()
          mapper.writeValue(writer, copySpectrum)

          val content = writer.toString
          authenticate().contentType("application/json; charset=UTF-8").body(content).when().post("/spectra").`then`().statusCode(400)
        }

        "fails if compound is null" in {
          val copySpectrum = new Spectrum(curatedRecords.head)
          copySpectrum.setCompound(null)

          val writer = new StringWriter()
          mapper.writeValue(writer, copySpectrum)

          val content = writer.toString
          authenticate().contentType("application/json; charset=UTF-8").body(content).when().post("/spectra").`then`().statusCode(400)
        }

        "fails if id is empty" in {
          val copySpectrum = new Spectrum(curatedRecords.head)
          copySpectrum.setId("")
          logger.info(s"${copySpectrum.getId}")

          val writer = new StringWriter()
          mapper.writeValue(writer, copySpectrum)

          val content = writer.toString
          logger.info(s"${content}")
          authenticate().contentType("application/json; charset=UTF-8").body(content).when().post("/spectra").`then`().statusCode(400)
        }
      }

      "we should be able to reset the repository" in {
        transactionTemplate.execute{ x =>
          spectrumRepository.deleteAll()
          Hibernate.initialize()
          x
        }


        eventually(timeout(10 seconds)) {
          assert(spectrumRepository.count() == 0)
        }
      }

      "we should be able to add spectra using POST at /rest/spectra with authentication" in {
        val countBefore = spectrumRepository.count()

        assert(countBefore == 0)

        exampleRecords.foreach { spectrum =>
          logger.debug("starting post request")
          authenticate().contentType("application/json; charset=UTF-8").body(spectrum).when().post("/spectra").`then`().statusCode(200)
        }

        eventually(timeout(10 seconds)) {
          val countAfter = spectrumRepository.count()
          assert(countAfter - exampleRecords.length == countBefore)
        }
      }

      "we should be able to query all the spectra using GET at /rest/spectra" in {
        val exampleRecords = given().contentType("application/json; charset=UTF-8").when().get("/spectra").`then`().statusCode(200).extract().body().as(classOf[Array[Spectrum]])
        assert(spectrumRepository.count() == exampleRecords.length)
      }

      "we should be able to test our pagination, while using GET at /rest/spectra?size=10 to 10 records" in {
        val exampleRecords = given().contentType("application/json; charset=UTF-8").when().get("/spectra?size=10").`then`().statusCode(200).extract().body().as(classOf[Array[Spectrum]])
        assert(10 == exampleRecords.length)
      }

      "we should be able to test our pagination, while using GET at /rest/spectra?size=10&page=1 to 10 records" in {
        val firstRecords = given().contentType("application/json; charset=UTF-8").when().get("/spectra?size=10").`then`().statusCode(200).extract().body().as(classOf[Array[Spectrum]])
        assert(10 == firstRecords.length)

        val exampleRecords = given().contentType("application/json; charset=UTF-8").when().get("/spectra?size=10&page=1").`then`().statusCode(200).extract().body().as(classOf[Array[Spectrum]])
        assert(10 == exampleRecords.length)

        exampleRecords.foreach(spectrum => {
          assert(!firstRecords.contains(spectrum))
        })
      }

      //wont work correclty as this test is dependent on deleting nothing
//      "we need to be authenticated to delete spectra " in {
//        given().when().delete(s"/spectra/111").`then`().statusCode(401)
//        authenticate("test", "test-secret").when().delete(s"/spectra/111").`then`().statusCode(200)
//      }

     "we should be able to delete a spectra using DELETE at /rest/spectra" in {
        val firstRecords = given().contentType("application/json; charset=UTF-8").when().get("/spectra?size=10").`then`().statusCode(200).extract().body().as(classOf[Array[Spectrum]])
        val countBefore = spectrumRepository.count()

        for (spec <- firstRecords) {
          authenticate().when().delete(s"/spectra/${spec.getId}").`then`().statusCode(200)
        }

        eventually(timeout(10 seconds)) {
          val countAfter = spectrumRepository.count()
          assert(countBefore - countAfter == 10)
        }
      }

      "we should be able to execute custom name subqueries and counts at /rest/spectra/search using GET" in {
        val exampleRecords = given().contentType("application/json; charset=UTF-8").when().get("/spectra/search?query=compound.names.name:'Trigenolline'").`then`().contentType(MediaType.APPLICATION_JSON_VALUE).statusCode(200).extract().body().as(classOf[Array[Spectrum]])

        assert(exampleRecords.length == 1)

        val count = authenticate().contentType("application/json: charset=UTF-8").when().get("/spectra/search/count?query=compound.names.name:'Trigenolline'").`then`().contentType(MediaType.APPLICATION_JSON_VALUE).statusCode(200).extract().as(classOf[Int])
        assert(count == 1)
      }

      "we should be able to execute custom name queries at /rest/spectra/search using GET" ignore {
        val exampleRecords = given().contentType("application/json; charset=UTF-8").when().get("/spectra/search?query=compound.names.name:'Trigenolline'").`then`().contentType(MediaType.APPLICATION_JSON_VALUE).statusCode(200).extract().body().as(classOf[Array[Spectrum]])
        assert(exampleRecords.length == 1)

        val count = authenticate().contentType("application/json: charset=UTF-8").when().get("/spectra/search/count?query=compound.names.name:'Trigenolline'").`then`().contentType(MediaType.APPLICATION_JSON_VALUE).statusCode(200).extract().as(classOf[Int])
        assert(count == 1)
      }

      "we should be able to execute custom metadata queries at /rest/spectra/search using GET" in {
        val exampleRecords = given().contentType("application/json; charset=UTF-8").when().get("/spectra/search?query=metaData.name:'ion mode' and metaData.value:'negative'").`then`().contentType(MediaType.APPLICATION_JSON_VALUE).statusCode(200).extract().body().as(classOf[Array[Spectrum]])
         assert(exampleRecords.length == 20)
        logger.info(s"${exampleRecords.length}")

        val count = authenticate().contentType("application/json: charset=UTF-8").when().get("/spectra/search/count?query=metaData.name:'ion mode' and metaData.value:'negative'").`then`().contentType(MediaType.APPLICATION_JSON_VALUE).statusCode(200).extract().as(classOf[Long])
        logger.info(s"${count}")
        assert(count == 20)
      }


      "we should be able to execute the same query several times and receive always the same result" must {

        "support pageable sizes of 1" in {
          var last: Spectrum = null
          var fetchedLast = false

          for (i <- 1 to 25) {
            val result: Array[Spectrum] = given().contentType("application/json; charset=UTF-8").when().get("/spectra/search?size=1&page=2&query=metaData.name:'ion mode' and metaData.value:'negative'").`then`().contentType(MediaType.APPLICATION_JSON_VALUE).statusCode(200).extract().body().as(classOf[Array[Spectrum]])
            assert(result.length == 1)

            val current = result(0)

            if (last == null) {
              last = current
              assert(!fetchedLast)
              fetchedLast = true
            } else {
              assert(last.getId == current.getId)
            }
          }
        }

      }
      "we should be able to get a query count without providing a query" in {
        val count = authenticate().contentType("application/json: charset=UTF-8").when().get("/spectra/search/count").`then`().contentType(MediaType.APPLICATION_JSON_VALUE).statusCode(200).extract().as(classOf[Int])
        assert(count == 49)
      }

      "we should be able to get a query count with an empty query" in {
        val count = authenticate().contentType("application/json: charset=UTF-8").when().get("/spectra/search/count?query=").`then`().contentType(MediaType.APPLICATION_JSON_VALUE).statusCode(200).extract().as(classOf[Int])
        assert(count == 49)
      }

      "we should be able to update a spectra with new properties" in {
        val spectrum = given().contentType("application/json; charset=UTF-8").when().get("/spectra?size=1").`then`().contentType(MediaType.APPLICATION_JSON_VALUE).statusCode(200).extract().body().as(classOf[Array[Spectrum]]).head
        val copySpectrum = new Spectrum(spectrum)
        copySpectrum.getSplash.setSplash("tada")
        val countBefore = spectrumRepository.count()

        authenticate().body(copySpectrum).when().contentType("application/json; charset=UTF-8").post("/spectra").`then`().log().all(true).contentType(MediaType.APPLICATION_JSON_VALUE).statusCode(200)

        eventually(timeout(10 seconds)) {
          val countAfter = spectrumRepository.count()
          val spectrumAfterUpdate = given().when().contentType("application/json; charset=UTF-8").get(s"/spectra/${copySpectrum.getId}").`then`().contentType(MediaType.APPLICATION_JSON_VALUE).statusCode(200).extract().body().as(classOf[Spectrum])

          assert(spectrumAfterUpdate.getSplash.getSplash == copySpectrum.getSplash.getSplash)
          assert(countBefore == countAfter)
        }
      }

      "we should be able to receive a spectra by it's ID using GET at /rest/spectra/{id}" in {
        val spectrum = given().contentType("application/json; charset=UTF-8").when().get("/spectra?size=1").`then`().contentType(MediaType.APPLICATION_JSON_VALUE).statusCode(200).extract().body().as(classOf[Array[Spectrum]]).head

        val spectrumByID = given().contentType("application/json; charset=UTF-8").when().get(s"/spectra/${spectrum.getId}").`then`().contentType(MediaType.APPLICATION_JSON_VALUE).statusCode(200).extract().body().as(classOf[Spectrum])

        assert(spectrum.getId.equals(spectrumByID.getId))
      }

      "if a spectra doesn't exist at /rest/spectra/{id}, we should receive a 404 " in {
        given().contentType("application/json; charset=UTF-8").when().get(s"/spectra/TADA1234").`then`().statusCode(404)
      }

      "we should be able to move a spectrum from one id to another using PUT as /rest/spectra " in {
        val spectrum = given().contentType("application/json; charset=UTF-8").when().get("/spectra?size=1").`then`().contentType(MediaType.APPLICATION_JSON_VALUE).statusCode(200).extract().body().as(classOf[Array[Spectrum]]).head

        val spectrumByID = given().contentType("application/json; charset=UTF-8").when().get(s"/spectra/${spectrum.getId}").`then`().contentType(MediaType.APPLICATION_JSON_VALUE).statusCode(200).extract().body().as(classOf[Spectrum])

        val spectrumIdMoved = authenticate().contentType("application/json; charset=UTF-8").when().body(spectrumByID).put(s"/spectra/${spectrum.getId}").`then`().contentType(MediaType.APPLICATION_JSON_VALUE).statusCode(200).extract().body().as(classOf[Spectrum])

        eventually(timeout(10 seconds)) {
          given().contentType("application/json; charset=UTF-8").when().get(s"/spectra/${spectrum.getId}").`then`().contentType(MediaType.APPLICATION_JSON_VALUE).statusCode(200)
        }
      }

      "we need to be authenticated for PUT requests" in {
        given().contentType("application/json; charset=UTF-8").when().put(s"/spectra/TADA_NEW_ID").`then`().statusCode(401)
      }

      "we should be able to update a spectrum at a given path using PUT at /rest/spectra " in {
        val spectrum = given().contentType("application/json; charset=UTF-8").when().get("/spectra?size=1").`then`().contentType(MediaType.APPLICATION_JSON_VALUE).statusCode(200).extract().body().as(classOf[Array[Spectrum]]).head

        val spectrumByID = given().contentType("application/json; charset=UTF-8").when().get(s"/spectra/${spectrum.getId}").`then`().contentType(MediaType.APPLICATION_JSON_VALUE).statusCode(200).extract().body().as(classOf[Spectrum])

        val spectrumIdMoved = authenticate().contentType("application/json; charset=UTF-8").when().body(spectrumByID).put(s"/spectra/TADA_NEW_ID").`then`().contentType(MediaType.APPLICATION_JSON_VALUE).statusCode(200).extract().body().as(classOf[Spectrum])

        val spectrumByIDNew = given().contentType("application/json; charset=UTF-8").when().get(s"/spectra/TADA_NEW_ID").`then`().contentType(MediaType.APPLICATION_JSON_VALUE).statusCode(200).extract().body().as(classOf[Spectrum])

        eventually(timeout(10 seconds)) {
          // should not exist anymore
          given().contentType("application/json; charset=UTF-8").when().get(s"/spectra/${spectrum.getId}").`then`().statusCode(404)
        }
      }

      "we should be able to delete our updated spectrum using DELETE at /rest/spectra" in {
        val repositoryCount = spectrumRepository.count()

        authenticate().when().delete(s"/spectra/TADA_NEW_ID").`then`().statusCode(200)

        eventually(timeout(10 seconds)) {
          //should not exist anymore
          given().contentType("application/json; charset=UTF-8").when().get(s"/spectra/TADA_NEW_ID").`then`().statusCode(404)

          assert(spectrumRepository.count() == repositoryCount - 1)
        }
      }
    }

    "we must be able to support different content types" must {
      "/spectra" should {
        "support application/json" in {
          given().contentType("application/json; charset=UTF-8").when().get("/spectra").`then`().contentType(MediaType.APPLICATION_JSON_VALUE).statusCode(200)
        }

        "text/msp must produce a msp file" in {
          given().header("accept", "text/msp").when().log().all(true).get("/spectra").`then`().contentType("text/msp").statusCode(200).log().all(true)
        }

        "text/msp must produce a msp file with size set" in {
          given().header("accept", "text/msp").when().log().all(true).get("/spectra?size=2").`then`().contentType("text/msp").statusCode(200).log().all(true)
        }

        "text/msp must produce a msp file with pagination" in {
          given().header("accept", "text/msp").when().log().all(true).get("/spectra?size=2&page=1").`then`().contentType("text/msp").statusCode(200).log().all(true)
        }
      }

      "/spectra/id" should {
        "application/json must produce a json file" in {
          val exampleRecords = given().contentType("application/json; charset=UTF-8").when().get("/spectra").`then`().contentType(MediaType.APPLICATION_JSON_VALUE).statusCode(200)
        }

        "text/msp must produce a msp file" in {
          val spec = given().contentType("application/json; charset=UTF-8").when().get("/spectra").`then`().contentType(MediaType.APPLICATION_JSON_VALUE).statusCode(200).extract().body().as(classOf[Array[Spectrum]])(0)
          given().header("accept", "text/msp").when().log().all(true).get(s"/spectra/${spec.getId}").`then`().log().all(true).contentType("text/msp").statusCode(200)
        }
      }

      "/spectra/search" should {
        "application/json must produce a json file" in {
          val exampleRecords = given().contentType("application/json; charset=UTF-8").when().get("/spectra/search?query=metaData.name:'ion mode' and metaData.value:'negative'").`then`().contentType(MediaType.APPLICATION_JSON_VALUE).statusCode(200)
        }

        "text/msp must produce a msp file" in {
          given().header("accept", "text/msp").when().log().all(true).get("/spectra/search?query=metaData.name:'ion mode' and metaData.value:'negative'").`then`().log().all(true).contentType("text/msp").statusCode(200)
        }

        "text/msp must produce a msp file with size set" in {
          given().header("accept", "text/msp").when().log().all(true).get("/spectra/search?size=2&query=metaData.name:'ion mode' and metaData.value:'negative'").`then`().log().all(true).contentType("text/msp").statusCode(200)
        }

        "text/msp must produce a msp file with pagination" in {
          given().header("accept", "text/msp").when().log().all(true).get("/spectra/search?size=2&page=1&query=metaData.name:'ion mode' and metaData.value:'negative'").`then`().log().all(true).contentType("text/msp").statusCode(200)
        }

        "we should be able to execute" in {
          given().when().log().all(true).get("/spectra/search?query=metaData.name:'collision energy' and metaData.value:'35%'").`then`().log().all(true).statusCode(200)
        }
      }


      "if specified the server should stay online, this can be done using the env variable 'keep.server.running=true' " in {
        if (keepRunning) {
          while (keepRunning) {
            logger.warn("waiting forever till you kill me!")
            Thread.sleep(300000); // Every 5 minutes
          }
        }
      }
    }
  }

  /**
    * object to use for gets
    *
    * @return
    */
  override def getValue: Spectrum = {
    val spectrum = monaMapper.readValue(new InputStreamReader(getClass.getResourceAsStream("/monaRecord.json")), new TypeReference[Spectrum] {})
    spectrum
  }

  /**
    * returns an id for us for testing
    *
    * @return
    */
  override def getId: String = getValue.getId

  override val requiresAuthForAllRequests: Boolean = false
}
