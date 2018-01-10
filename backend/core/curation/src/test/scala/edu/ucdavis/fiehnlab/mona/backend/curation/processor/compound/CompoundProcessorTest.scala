package edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound

import java.io.InputStreamReader

import edu.ucdavis.fiehnlab.mona.backend.core.domain._
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.config.RestClientConfig
import org.junit.runner.RunWith
import org.openscience.cdk.interfaces.IAtomContainer
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator
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
@SpringBootTest(classes = Array(classOf[CompoundTestApplication], classOf[RestClientConfig]))
class CompoundProcessorTest extends WordSpec {

  val reader: JSONDomainReader[Spectrum] = JSONDomainReader.create[Spectrum]

  @Autowired
  val compoundProcessor: CompoundProcessor = null

  @Autowired
  val compoundConversion: CompoundConversion = null

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

    "NOT process FIO00043 InChI" in {
      val impacts: ArrayBuffer[Impact] = ArrayBuffer[Impact]()
      val compound: Compound = Compound(
        "InChI=1S/C27H45NO/c1-16-5-8-23-17(2)25-24(28(23)15-16)14-22-20-7-6-18-13-19(29)9-11-26(18,3)21(20)10-12-27(22,25)4/h16-25,29H,5-15H2,1-4H4",
        null, Array.empty[MetaData], null, Array.empty[Names], Array.empty[Tags], computed = false, null)

      val (molDefinition, molecule): (String, IAtomContainer) = compoundProcessor.process(compound, "FIO00043", impacts)
      assert(molDefinition == null)
      assert(molecule == null)
      assert(impacts.length == 1)
    }

    "NOT process FIO00043 SMILES" in {
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

    "handle MOL data with explicit hydrogens" in {
      val compound: Compound = Compound(null, null, Array.empty[MetaData],
        "\n  CDK     0110182156\n\n 40 44  0  0  0  0  0  0  0  0999 V2000\n    3.9886   -2.7270    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    0.7120    3.1300    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    0.7570   -2.8860    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   -0.5860    3.8720    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   -0.5410   -3.6360    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    0.7340    0.1360    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    3.3400    0.1490    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    2.0440    0.8780    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    0.7270    1.6260    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    0.7570   -1.3870    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    2.0520   -0.6190    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   -1.8810    3.1040    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   -1.8390   -2.8860    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   -0.5560    0.8590    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   -1.8660    1.6010    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   -1.8390   -1.3870    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n   -0.5410   -0.6380    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    3.3690   -1.3610    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0\n   -3.1881    3.8399    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n   -3.3162   -2.6254    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n   -3.3490    0.1960    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n    2.0604   -2.1294    0.0000 H   0  0  0  0  0  0  0  0  0  0  0  0\n    3.3450    0.1413    0.0000 H   0  0  0  0  0  0  0  0  0  0  0  0\n   -2.3524   -4.2954    0.0000 H   0  0  0  0  0  0  0  0  0  0  0  0\n   -3.2315   -1.9446    0.0000 H   0  0  0  0  0  0  0  0  0  0  0  0\n    5.4814   -2.8735    0.0000 H   0  0  0  0  0  0  0  0  0  0  0  0\n    2.5357   -3.1000    0.0000 H   0  0  0  0  0  0  0  0  0  0  0  0\n    4.1036   -4.2226    0.0000 H   0  0  0  0  0  0  0  0  0  0  0  0\n    2.0057    3.8892    0.0000 H   0  0  0  0  0  0  0  0  0  0  0  0\n    2.0562   -3.6358    0.0000 H   0  0  0  0  0  0  0  0  0  0  0  0\n   -0.5980    5.3720    0.0000 H   0  0  0  0  0  0  0  0  0  0  0  0\n   -0.5410   -5.1360    0.0000 H   0  0  0  0  0  0  0  0  0  0  0  0\n    1.2320    1.5509    0.0000 H   0  0  0  0  0  0  0  0  0  0  0  0\n   -0.4307    1.0813    0.0000 H   0  0  0  0  0  0  0  0  0  0  0  0\n    4.8398    0.1707    0.0000 H   0  0  0  0  0  0  0  0  0  0  0  0\n    3.3254    1.6489    0.0000 H   0  0  0  0  0  0  0  0  0  0  0  0\n    3.5227    0.6263    0.0000 H   0  0  0  0  0  0  0  0  0  0  0  0\n    2.5479    2.2908    0.0000 H   0  0  0  0  0  0  0  0  0  0  0  0\n   -3.2043    5.3398    0.0000 H   0  0  0  0  0  0  0  0  0  0  0  0\n   -4.2805   -3.7743    0.0000 H   0  0  0  0  0  0  0  0  0  0  0  0\n  4  2  2  0  0  0  0 \n  5  3  2  0  0  0  0 \n  7  6  1  0  0  0  0 \n  9  2  1  0  0  0  0 \n  9  8  1  0  0  0  0 \n 10  3  1  0  0  0  0 \n 11  8  1  0  0  0  0 \n 11 10  1  0  0  0  0 \n 12  4  1  0  0  0  0 \n 13  5  1  0  0  0  0 \n 14  9  2  0  0  0  0 \n 15 12  2  0  0  0  0 \n 15 14  1  0  0  0  0 \n 16 13  1  0  0  0  0 \n 17  6  1  0  0  0  0 \n 17 10  1  0  0  0  0 \n 17 14  1  6  0  0  0 \n 17 16  1  0  0  0  0 \n 18  1  1  0  0  0  0 \n 18  7  1  0  0  0  0 \n 18 11  1  0  0  0  0 \n 19 12  1  0  0  0  0 \n 20 13  1  0  0  0  0 \n 21 15  1  0  0  0  0 \n 21 16  1  0  0  0  0 \n 10 22  1  1  0  0  0 \n 11 23  1  6  0  0  0 \n 13 24  1  1  0  0  0 \n 16 25  1  1  0  0  0 \n  1 26  1  0  0  0  0 \n  1 27  1  0  0  0  0 \n  1 28  1  0  0  0  0 \n  2 29  1  0  0  0  0 \n  3 30  1  0  0  0  0 \n  4 31  1  0  0  0  0 \n  5 32  1  0  0  0  0 \n  6 33  1  0  0  0  0 \n  6 34  1  0  0  0  0 \n  7 35  1  0  0  0  0 \n  7 36  1  0  0  0  0 \n  8 37  1  0  0  0  0 \n  8 38  1  0  0  0  0 \n 19 39  1  0  0  0  0 \n 20 40  1  0  0  0  0 \nM  END\n",
        Array.empty[Names], Array.empty[Tags], computed = false, null)

      val (molDefinition, molecule): (String, IAtomContainer) = compoundProcessor.process(compound, "Morphine")
      assert(molDefinition != null)
      assert(molecule != null)

      assert(molDefinition.split('\n')(3).trim().split(' ')(0).toInt == 21)
      assert(AtomContainerManipulator.getImplicitHydrogenCount(molecule) == AtomContainerManipulator.getTotalHydrogenCount(molecule))
    }

    "handle SMILES with explicit hydrogens" in {
      val compound: Compound = Compound(null, null,
        Array(MetaData("none", computed = false, hidden = false, "SMILES", null, null, null, "[H]C(=C([H])C([H])([H])C([H])([H])C([H])([H])C([H])([H])C([H])([H])[H])C([H])([H])C([H])=C([H])C([H])([H])C([H])([H])C([H])([H])C([H])([H])C([H])([H])C([H])([H])C([H])([H])C([H])([H])C([H])([H])C([H])([H])C([H])([H])C(=O)OC([H])(C([H])([H])OC(=O)C([H])([H])C([H])([H])C([H])([H])C([H])([H])C([H])([H])C([H])([H])C([H])([H])C([H])([H])C([H])([H])C([H])([H])C([H])([H])C([H])([H])C([H])([H])C([H])([H])C([H])([H])C([H])([H])C([H])([H])C([H])([H])C([H])([H])C([H])([H])C([H])([H])C([H])([H])C([H])([H])[H])C([H])([H])OP(=O)([O-])OC([H])([H])C([H])([H])[N+](C([H])([H])[H])(C([H])([H])[H])C([H])([H])[H]")),
        null, Array.empty[Names], Array.empty[Tags], computed = false, null)

      val (molDefinition, molecule): (String, IAtomContainer) = compoundProcessor.process(compound, "LipidBlast072690")
      assert(molDefinition != null)
      assert(molecule != null)

      assert(molDefinition.split('\n')(3).trim().split(' ')(0).toInt == 64)
      assert(AtomContainerManipulator.getImplicitHydrogenCount(molecule) == AtomContainerManipulator.getTotalHydrogenCount(molecule))
    }
  }
}