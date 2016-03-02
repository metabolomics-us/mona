package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client

import java.io.{File, FileReader}

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.ISpectrumRepositoryCustom
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.Application
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.config.RestClientConfig
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.springframework.beans.factory.annotation.{Value, Autowired}
import org.springframework.boot.test.{IntegrationTest, SpringApplicationConfiguration}
import org.springframework.context.annotation.{Primary, Bean}
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.web.WebAppConfiguration

/**
  * Created by wohlgemuth on 3/2/16.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[Application], classOf[RestClientConfig]))
@WebAppConfiguration
@IntegrationTest(Array("server.port:0"))
class GenericRestClientTest extends FunSuite with BeforeAndAfter {

  @Autowired
  val spectrumRestClient: GenericRestClient[Spectrum, String] = null


  @Autowired
  val spectrumRepository: ISpectrumRepositoryCustom = null


  @Value( """${local.server.port}""")
  val port: Int = 0

  @Bean
  @Primary
  def monaServerUrl = s"http://localhost:$port/rest/spectra"

  //required for spring and scala tes
  new TestContextManager(this.getClass()).prepareTestInstance(this)

  val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new FileReader(new File("src/test/resources/monaRecords.json")))

  /**
    * some test data to work with
    */
  before {
    for (spec <- exampleRecords) {
      spectrumRepository.save(spec)
    }
  }

  /**
    * clean the system
    */
  after {
    spectrumRepository.deleteAll()
  }

  test("testCount") {
    assert(spectrumRestClient.count() == 58)
  }

  test("testCount$default$1") {
    val test = spectrumRestClient.list().head
    assert(spectrumRestClient.count(Some(s"""{"_id":"${test.id}"}""")) == 1)
  }

  test("testUpdate") {
    val test = spectrumRestClient.list().head

    val result = spectrumRestClient.update(test, "newTestId")

    assert(result.id == "newTestId")

    assert(spectrumRepository.findOne("newTestId").id == "newTestId")

  }

  test("testGet") {

    val spectrum = spectrumRestClient.get(exampleRecords.head.id)

    assert(spectrum.id == exampleRecords.head.id)
  }

  test("testDelete") {

    val countBefore = spectrumRestClient.count()

    spectrumRestClient.delete(exampleRecords.head.id)

    val countAfter = spectrumRestClient.count()

    assert(countBefore - countAfter == 1)

  }

  test("testList") {
    val data = spectrumRestClient.list()
    assert(data.length == exampleRecords.length)
  }

  test("testList$default$1") {
    fail()
  }

  test("testList$default$2") {
    fail()
  }

  test("testList$default$3") {
    fail()
  }

  test("testAdd") {
    fail()
  }

}
