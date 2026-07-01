package edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound

import java.io.{StringReader, StringWriter}
import com.typesafe.scalalogging.LazyLogging
import net.sf.jniinchi.INCHI_RET
import org.openscience.cdk.exception.{CDKException, InvalidSmilesException}
import org.openscience.cdk.graph.ConnectivityChecker
import org.openscience.cdk.inchi.{InChIGeneratorFactory, InChIToStructure}
import org.openscience.cdk.interfaces.{IAtomContainer, IAtomContainerSet}
import org.openscience.cdk.io.{MDLV2000Reader, MDLV2000Writer}
import org.openscience.cdk.layout.StructureDiagramGenerator
import org.openscience.cdk.smiles.{SmiFlavor, SmilesGenerator, SmilesParser}
import org.openscience.cdk.tools.CDKHydrogenAdder
import org.openscience.cdk.tools.manipulator.{AtomContainerManipulator, MolecularFormulaManipulator}
import org.openscience.cdk.{AtomContainer, DefaultChemObjectBuilder}
import org.springframework.stereotype.Service

import scala.jdk.CollectionConverters._

/**
  * Created by sajjan on 8/31/16.
  */
@Service
class CompoundConversion extends LazyLogging {

  /**
    *
    * @param molecule
    * @return
    */
  def addHydrogens(molecule: IAtomContainer, addExplicitHydrogens: Boolean = false): IAtomContainer = {
    if (molecule != null) {
      val newMolecule: IAtomContainer = molecule

      AtomContainerManipulator.removeHydrogens(newMolecule)
      AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(newMolecule)
      CDKHydrogenAdder.getInstance(DefaultChemObjectBuilder.getInstance()).addImplicitHydrogens(newMolecule)

      if (addExplicitHydrogens) {
        AtomContainerManipulator.convertImplicitToExplicitHydrogens(newMolecule)
      }

      newMolecule
    } else {
      molecule
    }
  }


  /**
    *
    * @param smiles
    * @return
    */
  def smilesToMolecule(smiles: String, id: String): IAtomContainer = {
    try {
      val smilesParser: SmilesParser = new SmilesParser(DefaultChemObjectBuilder.getInstance())
      smilesParser.kekulise(false)
      val parsedSmile: IAtomContainer = smilesParser.parseSmiles(smiles)
      AtomContainerManipulator.suppressHydrogens(parsedSmile)
    } catch {
      case e: InvalidSmilesException =>
        logger.warn(s"$id: Invalid SMILES code '$smiles': ${e.getMessage}")
        null
      case e: Exception =>
        logger.warn(s"$id: Unknown SMILES error for '$smiles': ${e.getMessage}")
        null
    }
  }

  def smilesToMolDefinition(smiles: String, id: String): String = generateMolDefinition(smilesToMolecule(smiles, id), id)


  /**
    *
    * @param inchi
    * @return
    */
  def inchiToMolecule(inchi: String, id: String): IAtomContainer = {
    val inchiGeneratorFactory: InChIGeneratorFactory = InChIGeneratorFactory.getInstance()
    val inchiToStructure: InChIToStructure = inchiGeneratorFactory.getInChIToStructure(inchi, DefaultChemObjectBuilder.getInstance())

    val molecule: IAtomContainer = inchiToStructure.getAtomContainer
    val returnStatus = inchiToStructure.getReturnStatus

    if (returnStatus != INCHI_RET.OKAY && returnStatus != INCHI_RET.WARNING) {
      logger.error(s"$id: Structure generation failed: ${returnStatus.toString}\n[${inchiToStructure.getMessage}]\n[${inchiToStructure.getWarningFlags}]")
      null
    } else {
      if (returnStatus == INCHI_RET.WARNING) {
        logger.warn(s"$id: InChI warning: ${inchiToStructure.getMessage}")
      }

      AtomContainerManipulator.suppressHydrogens(molecule)
    }
  }

  def inchiToMolDefinition(inchi: String, id: String): String = generateMolDefinition(inchiToMolecule(inchi, id), id)


  /**
    *
    * @param molString
    * @return
    */
  def parseMolDefinition(molString: String, id: String): IAtomContainer = {
    logger.debug(s"$id: Receive MOL data: $molString")

    // Read MOL data
    val molecule: IAtomContainer = new MDLV2000Reader(new StringReader(molString)).read(new AtomContainer())
    AtomContainerManipulator.suppressHydrogens(molecule)
  }

  /**
    *
    * @param molecule
    * @return
    */
  def generateMolDefinition(molecule: IAtomContainer, id: String): String = {
    val stringWriter: StringWriter = new StringWriter()
    val mdlWriter: MDLV2000Writer = new MDLV2000Writer(stringWriter)
    mdlWriter.setWriteAromaticBondTypes(true)

    AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molecule)

    // Check connectivity
    val molSet: IAtomContainerSet = ConnectivityChecker.partitionIntoMolecules(molecule)

    if (molSet.getAtomContainerCount == 1) {
      val structureDiagramGenerator: StructureDiagramGenerator = new StructureDiagramGenerator()
      structureDiagramGenerator.setMolecule(molecule, true)
      structureDiagramGenerator.generateCoordinates()

      mdlWriter.writeMolecule(structureDiagramGenerator.getMolecule)
    }

    else {
      // TODO Improve handling disconnected structures
      logger.warn(s"$id: Generating MOL definition of disconnected molecules")

      val result: IAtomContainer = new AtomContainer

      molSet.atomContainers().asScala.foreach { x =>
        val structureDiagramGenerator: StructureDiagramGenerator = new StructureDiagramGenerator()
        structureDiagramGenerator.setMolecule(x, true)
        structureDiagramGenerator.generateCoordinates()

        result.add(structureDiagramGenerator.getMolecule)
      }

      mdlWriter.writeMolecule(result)
    }

    mdlWriter.close()
    stringWriter.toString
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
  def moleculeToInChI(molecule: IAtomContainer, id: String): String = moleculeToInChIAndInChIKey(molecule, id)._1

  /**
    *
    * @param molecule
    * @return
    */
  def moleculeToInChIKey(molecule: IAtomContainer, id: String): String = moleculeToInChIAndInChIKey(molecule, id)._2

  /**
    *
    * @param molecule
    * @return
    */
  def moleculeToInChIAndInChIKey(molecule: IAtomContainer, id: String): (String, String) = {
    try {
      val inchiGenerator = InChIGeneratorFactory.getInstance().getInChIGenerator(molecule)
      logger.info(s"$id: InChI conversion is: ${inchiGenerator.getReturnStatus} - ${inchiGenerator.getMessage}")
      (inchiGenerator.getInchi, inchiGenerator.getInchiKey)
    } catch {
      case e: CDKException =>
        logger.error(s"$id: CDK InChIGenerator failed for molecule: ${e.getMessage}", e)
        (null, null)

      case e: Exception =>
        logger.error(s"$id: Unexpected exception during InChIGenerator execution: ${e.getMessage}", e)
        (null, null)
    }
  }

  /**
    *
    * @param molecule
    * @return
    */
  def moleculeToSMILES(molecule: IAtomContainer): String = {
    try {
      SmilesGenerator.unique().create(molecule)
    } catch {
      case e: Exception => new SmilesGenerator(SmiFlavor.Unique | SmiFlavor.UseAromaticSymbols).create(addHydrogens(molecule))
    }
  }
}
