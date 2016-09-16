package edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound

import java.io.{StringReader, StringWriter}

import com.typesafe.scalalogging.LazyLogging
import net.sf.jniinchi.INCHI_RET
import org.openscience.cdk.exception.CDKException
import org.openscience.cdk.graph.ConnectivityChecker
import org.openscience.cdk.inchi.{InChIGeneratorFactory, InChIToStructure}
import org.openscience.cdk.interfaces.{IAtomContainer, IAtomContainerSet}
import org.openscience.cdk.io.{MDLReader, MDLV2000Writer}
import org.openscience.cdk.layout.StructureDiagramGenerator
import org.openscience.cdk.smiles.{SmilesGenerator, SmilesParser}
import org.openscience.cdk.tools.CDKHydrogenAdder
import org.openscience.cdk.tools.manipulator.{AtomContainerManipulator, MolecularFormulaManipulator}
import org.openscience.cdk.{AtomContainer, DefaultChemObjectBuilder}
import org.springframework.stereotype.Component

/**
  * Created by sajjan on 8/31/16.
  */
@Component
class CompoundConversion extends LazyLogging {

  /**
    *
    * @param smiles
    * @return
    */
  def smilesToMolecule(smiles: String): IAtomContainer = {
    val smilesParser: SmilesParser = new SmilesParser(DefaultChemObjectBuilder.getInstance())
    val molecule: IAtomContainer = smilesParser.parseSmiles(smiles)

    AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molecule)
    CDKHydrogenAdder.getInstance(DefaultChemObjectBuilder.getInstance()).addImplicitHydrogens(molecule)
    AtomContainerManipulator.convertImplicitToExplicitHydrogens(molecule)

    molecule
  }

  /**
    *
    * @param smiles
    * @return
    */
  def smilesToMolDefinition(smiles: String): String = generateMolDefinition(smilesToMolecule(smiles))


  def inchiToMolecule(inchi: String): IAtomContainer = {
    val inchiGeneratorFactory: InChIGeneratorFactory = InChIGeneratorFactory.getInstance()
    val inchiToStructure: InChIToStructure = inchiGeneratorFactory.getInChIToStructure(inchi, DefaultChemObjectBuilder.getInstance())

    val molecule: IAtomContainer = inchiToStructure.getAtomContainer

    AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molecule)
    CDKHydrogenAdder.getInstance(DefaultChemObjectBuilder.getInstance()).addImplicitHydrogens(molecule)
    AtomContainerManipulator.convertImplicitToExplicitHydrogens(molecule)

    val returnStatus = inchiToStructure.getReturnStatus

    if (returnStatus != INCHI_RET.OKAY && returnStatus != INCHI_RET.WARNING) {
      throw new CDKException(s"Structure generation failed: ${returnStatus.toString}\n [ ${inchiToStructure.getMessage}\t${inchiToStructure.getWarningFlags}")
    } else {
      if (returnStatus == INCHI_RET.WARNING) {
        logger.warn(s"InChI warning: ${inchiToStructure.getMessage}")
      }

      molecule
    }
  }

  /**
    *
    * @param inchi
    * @return
    */
  def inchiToMolDefinition(inchi: String): String = generateMolDefinition(inchiToMolecule(inchi))


  /**
    *
    * @param molString
    * @return
    */
  def parseMolDefinition(molString: String): IAtomContainer = {
    logger.debug(s"Receive MOL data: $molString")

    // Read MOL data
    val reader = new MDLReader(new StringReader(molString))
    val molecule: IAtomContainer = reader.read(new AtomContainer())

    // Add explicit hydrogens
    AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molecule)
    CDKHydrogenAdder.getInstance(DefaultChemObjectBuilder.getInstance()).addImplicitHydrogens(molecule)
    AtomContainerManipulator.convertImplicitToExplicitHydrogens(molecule)

    molecule
  }

  /**
    *
    * @param molecule
    * @return
    */
  def generateMolDefinition(molecule: IAtomContainer): String = {
    val stringWriter: StringWriter = new StringWriter()
    val mdlWriter: MDLV2000Writer = new MDLV2000Writer(stringWriter)

    val molSet: IAtomContainerSet = ConnectivityChecker.partitionIntoMolecules(molecule)

    if (molSet.getAtomContainerCount == 1) {
      // Generate 2D structure
      val structureDiagramGenerator: StructureDiagramGenerator = new StructureDiagramGenerator(molecule)
      structureDiagramGenerator.generateCoordinates()

      mdlWriter.writeMolecule(structureDiagramGenerator.getMolecule)
      mdlWriter.close()

      stringWriter.toString
    } else {
      throw new CDKException("Cannot generate MOL definition of disconnected molecules")
    }
  }


  /**
    *
    * @param molecule
    * @return
    */
  def moleculeToMolecularFormula(molecule: IAtomContainer): String = MolecularFormulaManipulator.getString(MolecularFormulaManipulator.getMolecularFormula(molecule))
  /**
    *
    * @param molecule
    * @return
    */
  def moleculeToTotalExactMass(molecule: IAtomContainer): Double = MolecularFormulaManipulator.getTotalExactMass(MolecularFormulaManipulator.getMolecularFormula(molecule))

  /**
    *
    * @param molecule
    * @return
    */
  def moleculeToInChI(molecule: IAtomContainer): String = moleculeToInChIAndInChIKey(molecule)._1

  /**
    *
    * @param molecule
    * @return
    */
  def moleculeToInChIKey(molecule: IAtomContainer): String = moleculeToInChIAndInChIKey(molecule)._2

  /**
    *
    * @param molecule
    * @return
    */
  def moleculeToInChIAndInChIKey(molecule: IAtomContainer): (String, String) = {
    val inchiGenerator = InChIGeneratorFactory.getInstance().getInChIGenerator(molecule)

    logger.debug(s"InChI conversion is: ${inchiGenerator.getReturnStatus} - ${inchiGenerator.getMessage}")

    (inchiGenerator.getInchi, inchiGenerator.getInchiKey)
  }

  /**
    *
    * @param molecule
    * @return
    */
  def moleculeToSMILES(molecule: IAtomContainer): String = SmilesGenerator.unique().create(molecule)
}