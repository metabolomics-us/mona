package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.controller

import java.io.{File, FileReader}

import com.fasterxml.jackson.databind.ObjectMapper
import com.jayway.restassured.RestAssured
import com.jayway.restassured.RestAssured._
import com.jayway.restassured.config.ObjectMapperConfig
import com.jayway.restassured.mapper.factory.Jackson2ObjectMapperFactory
import edu.ucdavis.fiehnlab.mona.backend.core.domain.HelperTypes.WrappedString
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.{JSONDomainReader, MonaMapper}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.ISpectrumMongoRepositoryCustom
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.MonaRestServer
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.controller.config.EmbeddedRestServerConfig
import org.junit.runner.RunWith
import org.scalatest.{WordSpec, FunSuite}
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.boot.test.{IntegrationTest, SpringApplicationConfiguration}
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.web.WebAppConfiguration
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config.EmbeddedMongoDBConfiguration

/**
  * Created by wohlgemuth on 3/8/16.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[MonaRestServer],classOf[EmbeddedRestServerConfig]))
@WebAppConfiguration
@IntegrationTest(Array("server.port:0"))
class MetaDataRestControllerTest extends WordSpec {

  @Value( """${local.server.port}""")
  val port: Int = 8080


  @Autowired
  val spectrumRepository: ISpectrumMongoRepositoryCustom = null

  //required for spring and scala tes
  new TestContextManager(this.getClass()).prepareTestInstance(this)


  "we will be connecting to the REST controller" when {

    RestAssured.config = RestAssured.config().objectMapperConfig(ObjectMapperConfig.objectMapperConfig().jackson2ObjectMapperFactory(new Jackson2ObjectMapperFactory {
      override def create(aClass: Class[_], s: String): ObjectMapper = MonaMapper.create
    }))

    RestAssured.baseURI = s"http://localhost:${port}/rest"

    "when connected we should be able to" should {

      spectrumRepository.deleteAll()


      //58 spectra for us to work with
      val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new FileReader(new File("src/test/resources/monaRecords.json")))
      assert(exampleRecords.length == 58)

      //save each record
      exampleRecords.foreach { x => spectrumRepository.save(x) }


      "we should be able to query all meta data names from the service" in {
        val result = given().contentType("application/json; charset=UTF-8").when().get("/metaData/names").then().statusCode(200).extract().body().as(classOf[Array[String]])
        assert(result.length == 44)
      }

      "we should be able to query all the meta data values for a specific name" in {
        val result = given().contentType("application/json; charset=UTF-8").when().body(WrappedString("authors")).post("/metaData/values").then().statusCode(200).extract().body().as(classOf[Array[String]])

        assert(result.length == 1)

        assert(result.head.equals("Mark Earll, Stephan Beisken, EMBL-EBI"))
      }
    }
  }
}
