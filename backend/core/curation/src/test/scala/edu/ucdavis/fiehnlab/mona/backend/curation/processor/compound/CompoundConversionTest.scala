package edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound

import org.openscience.cdk.interfaces.IAtomContainer
import org.scalatest.WordSpec

/**
  * Created by sajjan on 9/26/16.
  */
class CompoundConversionTest extends WordSpec {

  "CalculateConversionTest" should {
    val compoundConversion: CompoundConversion = new CompoundConversion

    val smiles_tests: Map[String, String] = Map(
      "CC(=O)N(CCCC1C(=O)NC(C(=O)NC(C(=O)NCC(=O)NCC(=O)NCC(=O)N1)CCCN(C(=O)C)[O-])CCCN(C(=O)C)[O-])[O-].[Fe+3][Na+]" -> "O=C1NCC(=O)NC(C(=O)NC(C(=O)NC(C(=O)NCC(=O)NC1)CCCN([O-])C(=O)C)CCCN([O-])C(=O)C)CCCN([O-])C(=O)C.[Na+][Fe+3]",
      "CC1C(C(C(C(O1)OCC2C(C(C(C(O2)OC3=CC(=C4C(=C3)OC(=CC4=O)C5=CC=C(C=C5)OC)O)O)O)O)O)O)O" -> "O=C1C=C(OC2=CC(OC3OC(COC4OC(C)C(O)C(O)C4O)C(O)C(O)C3O)=CC(O)=C12)C=5C=CC(OC)=CC5",

      // Aromatics
      "CCCCNC(=O)NS(=O)(=O)c1ccc(C)cc1" -> "O=C(NCCCC)NS(=O)(=O)c1ccc(cc1)C",
      "CC1CCN(CCCC(=O)c2ccc(F)cc2)CC1" -> "O=C(c1ccc(F)cc1)CCCN2CCC(C)CC2"
    )

    smiles_tests.foreach { case (key, value) =>

      s"generate the correct canonical SMILES for $key" in {
        val molecule: IAtomContainer = compoundConversion.parseMolDefinition(compoundConversion.smilesToMolDefinition(key))

        assert(compoundConversion.moleculeToSMILES(molecule) == value)
      }
    }
  }
}