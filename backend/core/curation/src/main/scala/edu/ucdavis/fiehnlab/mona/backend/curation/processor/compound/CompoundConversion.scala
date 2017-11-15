package edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound

import java.io.{StringReader, StringWriter}

import com.typesafe.scalalogging.LazyLogging
import net.sf.jniinchi.INCHI_RET
import org.openscience.cdk.exception.InvalidSmilesException
import org.openscience.cdk.graph.ConnectivityChecker
import org.openscience.cdk.inchi.{InChIGeneratorFactory, InChIToStructure}
import org.openscience.cdk.interfaces.{IAtomContainer, IAtomContainerSet}
import org.openscience.cdk.io.{MDLV2000Reader, MDLV2000Writer}
import org.openscience.cdk.layout.StructureDiagramGenerator
import org.openscience.cdk.smiles.{SmilesGenerator, SmilesParser}
import org.openscience.cdk.tools.CDKHydrogenAdder
import org.openscience.cdk.tools.manipulator.{AtomContainerManipulator, MolecularFormulaManipulator}
import org.openscience.cdk.{AtomContainer, DefaultChemObjectBuilder}
import org.springframework.stereotype.Service

import scala.collection.JavaConverters._

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
  def addExplicitHydrogens(molecule: IAtomContainer): IAtomContainer = {
    if (molecule != null) {
      val newMolecule: IAtomContainer = molecule

      AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(newMolecule)
      CDKHydrogenAdder.getInstance(DefaultChemObjectBuilder.getInstance()).addImplicitHydrogens(newMolecule)
      AtomContainerManipulator.convertImplicitToExplicitHydrogens(newMolecule)

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
  def smilesToMolecule(smiles: String): IAtomContainer = {
    try {
      val smilesParser: SmilesParser = new SmilesParser(DefaultChemObjectBuilder.getInstance())
      smilesParser.kekulise(false)
      smilesParser.parseSmiles(smiles)
    } catch {
      case e: InvalidSmilesException =>
        logger.error("Invalid SMILES Code")
        e.printStackTrace()
        null
      case e: Exception =>
        logger.error("Unknown SMILES Error")
        e.printStackTrace()
        null
    }
  }

  def smilesToMolDefinition(smiles: String): String = generateMolDefinition(smilesToMolecule(smiles))


  /**
    *
    * @param inchi
    * @return
    */
  def inchiToMolecule(inchi: String): IAtomContainer = {
    val inchiGeneratorFactory: InChIGeneratorFactory = InChIGeneratorFactory.getInstance()
    val inchiToStructure: InChIToStructure = inchiGeneratorFactory.getInChIToStructure(inchi, DefaultChemObjectBuilder.getInstance())

    val molecule: IAtomContainer = inchiToStructure.getAtomContainer
    val returnStatus = inchiToStructure.getReturnStatus

    if (returnStatus != INCHI_RET.OKAY && returnStatus != INCHI_RET.WARNING) {
      logger.error(s"Structure generation failed: ${returnStatus.toString}\n[${inchiToStructure.getMessage}]\n[${inchiToStructure.getWarningFlags}]")
      null
    } else {
      if (returnStatus == INCHI_RET.WARNING) {
        logger.warn(s"InChI warning: ${inchiToStructure.getMessage}")
      }
      molecule
    }
  }

  def inchiToMolDefinition(inchi: String): String = generateMolDefinition(inchiToMolecule(inchi))


  /**
    *
    * @param molString
    * @return
    */
  def parseMolDefinition(molString: String): IAtomContainer = {
    logger.debug(s"Receive MOL data: $molString")

    // Read MOL data
    new MDLV2000Reader(new StringReader(molString)).read(new AtomContainer())
  }

  /**
    *
    * @param molecule
    * @return
    */
  def generateMolDefinition(molecule: IAtomContainer): String = {
    val stringWriter: StringWriter = new StringWriter()
    val mdlWriter: MDLV2000Writer = new MDLV2000Writer(stringWriter)

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
      logger.warn("Generating MOL definition of disconnected molecules")

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
  def moleculeToSMILES(molecule: IAtomContainer): String = SmilesGenerator.generic().create(molecule)
}