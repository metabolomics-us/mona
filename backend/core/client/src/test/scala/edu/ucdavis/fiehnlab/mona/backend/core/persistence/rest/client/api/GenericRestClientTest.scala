package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.api

import java.io.InputStreamReader

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.config.RestClientTestConfig
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.boot.test.{SpringApplicationConfiguration, WebIntegrationTest}
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
  * Created by wohlgemuth on 3/2/16.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[RestClientTestConfig]))
@WebIntegrationTest(Array("server.port=44444"))
class GenericRestClientTest extends WordSpec {
  @Value( """${local.server.port}""")
  val port: Int = 0

  @Autowired
  val spectrumRestClient: GenericRestClient[Spectrum, String] = null

  //required for spring and scala tes
  new TestContextManager(this.getClass()).prepareTestInstance(this)

  /**
    * some test data to work with
    */
  "when we start a client" when {


    val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")))

    "we should be able to delete all data" in {
      spectrumRestClient.list().foreach(x =>
        spectrumRestClient.delete(x.id)
      )
    }

    "we should be able to add spectra" in {
      for (spec <- exampleRecords) {
        spectrumRestClient.add(spec)
      }
    }

    "we should have 58 spectra" in {
      assert(spectrumRestClient.count() == 58)
    }

    "it should be possible to execute count queries" ignore {
      val test = spectrumRestClient.list().head
      assert(spectrumRestClient.count(Some(s"""id==${test.id}""")) == 1)
    }

    "it should be possible to update values" in {
      val test = spectrumRestClient.list().toList.head

      val result = spectrumRestClient.update(test, "newTestId")

      assert(result.id == "newTestId")

      assert(spectrumRestClient.get("newTestId").id == "newTestId")

    }

    "it should be possible to get values" in {
      val records = spectrumRestClient.list().toList
      val spectrum = spectrumRestClient.get(records.head.id)

      assert(spectrum.id == records.head.id)
    }

    "it should be possible to list all values" in {
      val count = spectrumRestClient.list().foldLeft(0)((sum,_) => sum + 1)
      assert(count == 58)
    }


    "it should be possible to stream all values" in {
      val count = spectrumRestClient.list().foldLeft(0)((sum,_) => sum + 1)
      assert(count == 58)
    }



    "it should be possible to paginate" in {

      val count = spectrumRestClient.list(pageSize = Some(10)).foldLeft(0)((sum,_) => sum + 1)
      assert(count == 10)
    }


    "it should be possible to paginate over several pages" in {
      val dataFirst = spectrumRestClient.list(pageSize = Some(10), page = Some(0)).toList
      val dataSecond = spectrumRestClient.list(pageSize = Some(10), page = Some(1)).toList

      assert(dataFirst.length == 10)

      assert((dataFirst.toSet diff dataSecond.toSet).size == 10)

    }

    "it should be possible to execute queries - Warning ELASTIST SEARCH WILL RETURN WRONG COUNT,HENCE >=" in {
      val data = spectrumRestClient.list(Some(""" tags=q='text==LCMS' """))
      assert(data.toList.length >= exampleRecords.length)

    }


    "it should be possible to delete values" in {
      val records = spectrumRestClient.list()

      val countBefore = spectrumRestClient.count()

      spectrumRestClient.delete(records.head.id)

      val countAfter = spectrumRestClient.count()

      assert(countBefore - countAfter == 1)

    }
  }
}