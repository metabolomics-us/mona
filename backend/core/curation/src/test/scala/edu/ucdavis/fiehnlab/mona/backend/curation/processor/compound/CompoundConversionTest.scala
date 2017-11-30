package edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound

import org.openscience.cdk.interfaces.IAtomContainer
import org.scalatest.WordSpec

/**
  * Created by sajjan on 9/26/16.
  */
class CompoundConversionTest extends WordSpec {

  "CalculateConversionTest" should {
    val compoundConversion: CompoundConversion = new CompoundConversion

    "generate the correct canonical SMILES" in {
      val SMILES: String = "CC(=O)N(CCCC1C(=O)NC(C(=O)NC(C(=O)NCC(=O)NCC(=O)NCC(=O)N1)CCCN(C(=O)C)[O-])CCCN(C(=O)C)[O-])[O-].[Fe+3][Na+]"
      val molecule: IAtomContainer = compoundConversion.parseMolDefinition(compoundConversion.smilesToMolDefinition(SMILES))

      assert(compoundConversion.moleculeToSMILES(molecule) == SMILES)
    }
  }
}