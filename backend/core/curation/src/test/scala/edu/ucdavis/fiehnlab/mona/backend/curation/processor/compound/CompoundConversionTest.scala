package edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound

import org.openscience.cdk.interfaces.IAtomContainer
import org.scalatest.WordSpec

/**
  * Created by sajjan on 9/26/16.
  */
class CompoundConversionTest extends WordSpec {

  "CalculateConversionTest" should {
    val compoundConversion: CompoundConversion = new CompoundConversion

    "generate the correct canonical SMILES for CC(=O)N(CCCC1C(=O)NC(C(=O)NC(C(=O)NCC(=O)NCC(=O)NCC(=O)N1)CCCN(C(=O)C)[O-])CCCN(C(=O)C)[O-])[O-].[Fe+3][Na+]" in {
      val SMILES: String = "CC(=O)N(CCCC1C(=O)NC(C(=O)NC(C(=O)NCC(=O)NCC(=O)NCC(=O)N1)CCCN(C(=O)C)[O-])CCCN(C(=O)C)[O-])[O-].[Fe+3][Na+]"
      val molecule: IAtomContainer = compoundConversion.parseMolDefinition(compoundConversion.smilesToMolDefinition(SMILES))

      assert(compoundConversion.moleculeToSMILES(molecule) == SMILES)
    }

    "generate the correct canonical SMILES for CC1C(C(C(C(O1)OCC2C(C(C(C(O2)OC3=CC(=C4C(=C3)OC(=CC4=O)C5=CC=C(C=C5)OC)O)O)O)O)O)O)O" in {
      val SMILES: String = "CC1C(C(C(C(O1)OCC2C(C(C(C(O2)OC3=CC(=C4C(=C3)OC(=CC4=O)C5=CC=C(C=C5)OC)O)O)O)O)O)O)O"
      val molecule: IAtomContainer = compoundConversion.parseMolDefinition(compoundConversion.smilesToMolDefinition(SMILES))



      assert(compoundConversion.moleculeToSMILES(molecule) == SMILES)
    }

  }
}