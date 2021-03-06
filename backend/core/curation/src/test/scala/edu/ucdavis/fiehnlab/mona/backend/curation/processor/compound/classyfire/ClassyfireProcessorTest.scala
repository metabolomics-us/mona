package edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound.classyfire

import java.io.InputStreamReader

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.config.RestClientConfig
import edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound.CompoundTestApplication
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner

/**
  * Created by wohlgemuth on 5/5/16.
  */
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[CompoundTestApplication], classOf[RestClientConfig]))
class ClassyfireProcessorTest extends WordSpec with LazyLogging {

  val reader: JSONDomainReader[Spectrum] = JSONDomainReader.create[Spectrum]

  @Autowired
  val classyfireProcessor: ClassyfireProcessor = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "ClassyfireProcessorTest" should {
    val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")))
    val spectrumGiven: Spectrum = exampleRecords.head

    "process" in {
      assert(classyfireProcessor != null)

      if (classyfireProcessor.isReachable) {
        val output = classyfireProcessor.process(spectrumGiven)

        output.compound.foreach { compound =>
          assert(compound.classification.length > 0)
        }
      } else {
        logger.error("ClassyFire is offline - skipping test")
      }
    }
  }
}