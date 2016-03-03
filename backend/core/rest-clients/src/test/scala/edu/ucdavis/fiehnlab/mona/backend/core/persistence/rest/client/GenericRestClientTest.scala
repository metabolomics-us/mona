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
import org.springframework.context.annotation.{Import, Configuration, Primary, Bean}
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.web.WebAppConfiguration

/**
  * Created by wohlgemuth on 3/2/16.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[RestClientConfig],classOf[Application]))
@WebAppConfiguration
@IntegrationTest(Array("server.port:44444"))
class GenericRestClientTest extends FunSuite with BeforeAndAfter {

  @Autowired
  val spectrumRestClient: GenericRestClient[Spectrum, String] = null

  //required for spring and scala tes
  new TestContextManager(this.getClass()).prepareTestInstance(this)

  val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new FileReader(new File("src/test/resources/monaRecords.json")))

  /**
    * some test data to work with
    */
  before {
    for (spec <- exampleRecords) {
      spectrumRestClient.add(spec)
    }
  }

  /**
    * clean the system
    */
  after {
    spectrumRestClient.list().foreach(x =>
      spectrumRestClient.delete(x.id)
    )
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

    assert(spectrumRestClient.get("newTestId").id == "newTestId")

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
    val data = spectrumRestClient.list(pageSize = Some(10))
    assert(data.length == 10)
  }

  test("testList$default$2") {
    val dataFirst = spectrumRestClient.list(pageSize = Some(10), page = Some(0))
    val dataSecond = spectrumRestClient.list(pageSize = Some(10), page = Some(1))

    assert(dataFirst.length == 10)

    assert((dataFirst.toSet diff dataSecond.toSet).size == 10)
  }

  test("testList$default$3") {
    val data = spectrumRestClient.list(Some("""{"tags" : {$elemMatch : { text : "LCMS" } } }"""))
    assert(data.length == exampleRecords.length)

  }
}