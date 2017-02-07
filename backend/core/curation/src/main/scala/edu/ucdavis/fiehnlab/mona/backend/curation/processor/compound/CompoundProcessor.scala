package edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Compound, MetaData}
import edu.ucdavis.fiehnlab.mona.backend.curation.util.CommonMetaData
import org.openscience.cdk.interfaces.IAtomContainer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.{Component, Service}

/**
  * Created by sajjan on 9/27/16.
  */
@Service
class CompoundProcessor extends LazyLogging {

  @Autowired
  val molProcessor: CompoundMOLProcessor = null

  @Autowired
  val inchiProcessor: CompoundInChIProcessor = null

  @Autowired
  val smilesProcessor: CompoundSMILESProcessor = null

  def process(compound: Compound, id: String): (String, IAtomContainer) = {
    val molProcessorResult = molProcessor.process(compound, id)
    val inchiProcessorResult = inchiProcessor.process(compound, id)
    val smilesProcessorResult = smilesProcessor.process(compound, id)

    if (molProcessorResult._1 != null && molProcessorResult._2 != null) {
      molProcessorResult
    } else if (inchiProcessorResult._1 != null && inchiProcessorResult._2 != null) {
      inchiProcessorResult
    } else if (smilesProcessorResult._1 != null && smilesProcessorResult._2 != null) {
      smilesProcessorResult
    } else {
      logger.warn(s"$id: Unable to generate CDK molecule")
      (null, null)
    }
  }
}

@Component
trait AbstractCompoundProcessor extends LazyLogging {

  @Autowired
  val compoundConversion: CompoundConversion = null

  def process(compound: Compound, id: String): (String, IAtomContainer)
}


@Component
class CompoundMOLProcessor extends AbstractCompoundProcessor {

  def process(compound: Compound, id: String): (String, IAtomContainer) = {
    if (compound.molFile != null || compound.molFile == "") {
      logger.info(s"$id: Parsing MOL definition")
      (compound.molFile, compoundConversion.parseMolDefinition(compound.molFile))
    } else {
      logger.info(s"$id: No MOL definition found")
      (null, null)
    }
  }
}


@Component
class CompoundInChIProcessor extends AbstractCompoundProcessor {

  def process(compound: Compound, id: String): (String, IAtomContainer) = {
    val inchiMetaData: Option[MetaData] = compound.metaData.find(_.name.toLowerCase() == CommonMetaData.INCHI_CODE.toLowerCase())

    val inchi: String =
      if (compound.inchi != null && compound.inchi != "")
        compound.inchi
      else if (inchiMetaData.isDefined && inchiMetaData.get.value.toString != "")
        inchiMetaData.get.value.toString
      else
          null

    if (inchi != null) {
      logger.info(s"$id: Converting InChI to MOL definition...")

      val molecule: IAtomContainer = compoundConversion.inchiToMolecule(inchi)

      if (molecule != null) {
        logger.info(s"$id: InChI conversion successful")
        (compoundConversion.generateMolDefinition(molecule), molecule)
      } else {
        logger.warn(s"$id: InChI conversion failed")
        (null, null)
      }
    } else {
      logger.info(s"$id: No InChI found")
      (null, null)
    }
  }
}


@Component
class CompoundSMILESProcessor extends AbstractCompoundProcessor {

  def process(compound: Compound, id: String): (String, IAtomContainer) = {
    val smiles: Option[MetaData] = compound.metaData.find(_.name.toLowerCase() == CommonMetaData.SMILES.toLowerCase())

    // Parse SMILES
    if (smiles.isDefined && smiles.get.value.toString != "") {
      logger.info(s"$id: Converting SMILES to MOL definition")

      val molecule: IAtomContainer = compoundConversion.smilesToMolecule(smiles.get.value.toString)

      if (molecule != null) {
        logger.info(s"$id: Converting SMILES to MOL definition")
        (compoundConversion.generateMolDefinition(molecule), molecule)
      } else {
        logger.info(s"$id: SMILES conversion failed")
        (null, null)
      }
    } else {
      logger.info(s"$id: No SMILES found")
      (null, null)
    }
  }
}