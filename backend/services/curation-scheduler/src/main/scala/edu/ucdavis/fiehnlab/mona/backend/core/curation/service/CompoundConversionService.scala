package edu.ucdavis.fiehnlab.mona.backend.core.curation.service

import edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound.CompoundConversion
import org.openscience.cdk.interfaces.IAtomContainer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
  * Created by sajjan on 12/11/2017.
  */
@Service
class CompoundConversionService {

  @Autowired
  val compoundConversion: CompoundConversion = null


  private def generateCompoundSummary(molecule: IAtomContainer): CompoundSummary = {
    val (inchi, inchiKey): (String, String) = compoundConversion.moleculeToInChIAndInChIKey(molecule)

    CompoundSummary(
      inchi,
      inchiKey,
      compoundConversion.moleculeToSMILES(molecule),
      compoundConversion.moleculeToMolecularFormula(molecule),
      compoundConversion.moleculeToTotalExactMass(molecule),
      compoundConversion.generateMolDefinition(molecule)
    )
  }


  def parseSmiles(smiles: String): CompoundSummary =
    generateCompoundSummary(compoundConversion.smilesToMolecule(smiles))


  def parseInChI(inchi: String): CompoundSummary =
    generateCompoundSummary(compoundConversion.inchiToMolecule(inchi))

  def parseMol(mol: String): CompoundSummary =
    generateCompoundSummary(compoundConversion.parseMolDefinition(mol))
}


case class CompoundSummary(inchi: String,
                           inchiKey: String,
                           smiles: String,
                           molecularFormula: String,
                           totalExactMass: Double,
                           molData: String)