package edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound

import java.io.InputStreamReader

import edu.ucdavis.fiehnlab.mona.backend.core.domain._
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.config.RestClientConfig
import org.junit.runner.RunWith
import org.openscience.cdk.interfaces.IAtomContainer
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner

import scala.collection.mutable.ArrayBuffer

/**
  * Created by sajjan on 9/26/16.
  */
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[CompoundTestConfig], classOf[RestClientConfig]))
class CompoundProcessorTest extends WordSpec {

  val reader: JSONDomainReader[Spectrum] = JSONDomainReader.create[Spectrum]

  @Autowired
  val compoundProcessor: CompoundProcessor = null

  new TestContextManager(this.getClass).prepareTestInstance(this)


  "CalculateProcessorTest" should {
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
      assert(molDefinition != null)
      assert(molecule != null)
    }

    "not process FIO00043 InChI" in {
      val impacts: ArrayBuffer[Impact] = ArrayBuffer[Impact]()
      val compound: Compound = Compound(
        "InChI=1S/C27H45NO/c1-16-5-8-23-17(2)25-24(28(23)15-16)14-22-20-7-6-18-13-19(29)9-11-26(18,3)21(20)10-12-27(22,25)4/h16-25,29H,5-15H2,1-4H4",
        null, Array.empty[MetaData], null, Array.empty[Names], Array.empty[Tags], computed = false, null)

      val (molDefinition, molecule): (String, IAtomContainer) = compoundProcessor.process(compound, "FIO00043", impacts)
      assert(molDefinition == null)
      assert(molecule == null)
      assert(impacts.length == 1)
    }

    "not process FIO00043 SMILES" in {
      val impacts: ArrayBuffer[Impact] = ArrayBuffer[Impact]()
      val compound: Compound = Compound(null, null,
        Array(MetaData("none", computed = false, hidden = false, "SMILES", null, null, null, "C(C1)(C)CCC(C6C)N(C(C56)CC(C(C)54)C(C2)C(CC4)C(C)(C3)C(CC(O)C3)C2)2")),
        null, Array.empty[Names], Array.empty[Tags], computed = false, null)

      val (molDefinition, molecule): (String, IAtomContainer) = compoundProcessor.process(compound, "FIO00043", impacts)
      assert(molDefinition == null)
      assert(molecule == null)
      assert(impacts.length == 1)
    }

    "process HMDB00786_1122 InChIKey" in {
      val compound: Compound = Compound(null, "LFQSCWFLJHTTHZ-UHFFFAOYSA-N", Array(), null, Array.empty[Names], Array.empty[Tags], computed = false, null)

      val (molDefinition, molecule): (String, IAtomContainer) = compoundProcessor.process(compound, "HMDB00786_1122")
      assert(molDefinition != null)
      assert(molecule != null)
    }
  }
}