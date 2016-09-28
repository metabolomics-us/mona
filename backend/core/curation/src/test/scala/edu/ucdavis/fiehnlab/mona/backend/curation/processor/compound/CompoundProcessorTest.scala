package edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound

import java.io.InputStreamReader
import java.util.Collections

import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.domain._
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.config.RestClientTestConfig
import edu.ucdavis.fiehnlab.mona.backend.curation.TestConfig
import org.junit.runner.RunWith
import org.openscience.cdk.interfaces.IAtomContainer
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.{SpringApplicationConfiguration, WebIntegrationTest}
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
  * Created by sajjan on 9/26/16.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[RestClientTestConfig], classOf[TestConfig], classOf[JWTAuthenticationConfig]))
@WebIntegrationTest(Array("server.port=44444"))
class CompoundProcessorTest extends WordSpec {
  val reader = JSONDomainReader.create[Spectrum]

  @Autowired
  val compoundProcessor: CompoundProcessor = null

  new TestContextManager(this.getClass).prepareTestInstance(this)


  "CalculateCompoundPropertiesTest" should {

    val input = new InputStreamReader(getClass.getResourceAsStream("/monaRecord.json"))
    val spectrum: Spectrum = reader.read(input)

    "process" in {
      assert(compoundProcessor != null)

      val (molDefinition, molecule): (String, IAtomContainer) = compoundProcessor.process(spectrum.compound.head, spectrum.id)

      assert(molDefinition != null)
      assert(molecule != null)
    }

    "handle disconnected molecules without failing" in {
      val compound: Compound = Compound(
        "InChI=1S/2C44H76NO8P/c2*1-6-8-10-12-14-16-18-20-22-24-26-28-30-32-34-36-43(46)50-40-42(41-52-54(48,49)51-39-38-45(3,4)5)53-44(47)37-35-33-31-29-27-25-23-21-19-17-15-13-11-9-7-2/h18-21,24-27,30-33,42H,6-17,22-23,28-29,34-41H2,1-5H3;12,14,18,20,24-27,30-33,42H,6-11,13,15-17,19,21-23,28-29,34-41H2,1-5H3/b20-18-,21-19-,26-24-,27-25-,32-30-,33-31-;14-12-,20-18-,26-24-,27-25-,32-30-,33-31-",
        null, Array.empty[MetaData], null, Array.empty[Names], Array.empty[Tags], computed = false, null)

      val (molDefinition, molecule): (String, IAtomContainer) = compoundProcessor.process(compound, "UT001056")
    }
  }
}