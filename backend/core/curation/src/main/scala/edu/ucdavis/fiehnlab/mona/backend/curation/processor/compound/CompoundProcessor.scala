package edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Compound, Impacts, MetaData}
import edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound.cts.CTSLiteService
import edu.ucdavis.fiehnlab.mona.backend.curation.util.CommonMetaData
import org.openscience.cdk.interfaces.IAtomContainer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.{Component, Service}

import scala.collection.mutable.ArrayBuffer
import scala.jdk.CollectionConverters._

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

  @Autowired
  val inchikeyProcessor: CompoundInChIKeyProcessor = null


  def process(compound: Compound, id: String, impacts: ArrayBuffer[Impacts]): (String, IAtomContainer) = {

    def attempt(processor: AbstractCompoundProcessor): (String, IAtomContainer) =
      try {
        processor.process(compound, id, impacts)
      } catch {
        case e: Exception =>
          e.printStackTrace()
          (null, null)
      }

    def isValid(result: (String, IAtomContainer)): Boolean = result != null && result._1 != null && result._2 != null

    // Try each structure source in priority order, stopping at the first that yields a molecule.
    // The InChIKey lookup is an external call, so it only runs as a last resort when no structure
    // is already present on the record
    val sources: Seq[(String, AbstractCompoundProcessor)] = Seq(
      ("Using provided MOL definition", molProcessor),
      ("Using provided InChI to resolve MOL definition", inchiProcessor),
      ("Using provided SMILES to resolve MOL definition", smilesProcessor),
      ("Using provided InChIKey to resolve MOL definition", inchikeyProcessor)
    )

    sources.iterator
      .map { case (message, processor) => (message, attempt(processor)) }
      .find { case (_, result) => isValid(result) } match {
      case Some((message, result)) =>
        logger.info(s"$id: $message")
        result
      case None =>
        logger.warn(s"$id: Unable to generate CDK molecule")
        (null, null)
    }
  }

  def process(compound: Compound, id: String): (String, IAtomContainer) = process(compound, id, null)
}

@Component
trait AbstractCompoundProcessor extends LazyLogging {

  @Autowired
  val compoundConversion: CompoundConversion = null

  def process(compound: Compound, id: String, impacts: ArrayBuffer[Impacts]): (String, IAtomContainer)
}


@Component
class CompoundMOLProcessor extends AbstractCompoundProcessor {

  def process(compound: Compound, id: String, impacts: ArrayBuffer[Impacts]): (String, IAtomContainer) = {
    if (compound.getMolFile != null && !compound.getMolFile.isEmpty) {
      logger.info(s"$id: Parsing MOL definition")

      val molecule: IAtomContainer = compoundConversion.parseMolDefinition(compound.getMolFile)

      if (impacts != null && molecule == null) {
        impacts.append(new Impacts(-1, "MOL data could not be parsed"))
      }

      (compoundConversion.generateMolDefinition(molecule), molecule)
    } else {
      logger.info(s"$id: No MOL definition found")
      (null, null)
    }
  }
}


@Component
class CompoundInChIProcessor extends AbstractCompoundProcessor {

  def process(compound: Compound, id: String, impacts: ArrayBuffer[Impacts]): (String, IAtomContainer) = {
    val inchiMetaData: Option[MetaData] = compound.getMetaData.asScala.find(_.getName.toLowerCase == CommonMetaData.INCHI_CODE.toLowerCase)

    val inchi: String =
      if (compound.getInchi != null && !compound.getInchi.isEmpty)
        compound.getInchi
      else if (inchiMetaData.isDefined && inchiMetaData.get.getValue.toString != "")
        inchiMetaData.get.getValue.toString
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

        if (impacts != null) {
          impacts.append(new Impacts(-1, "InChI conversion failed"))
        }

        (null, null)
      }
    } else {
      logger.info(s"$id: No InChI found")
      (null, null)
    }
  }
}


@Component
class CompoundSMILESProcessor extends AbstractCompoundProcessor with LazyLogging {

  def process(compound: Compound, id: String, impacts: ArrayBuffer[Impacts]): (String, IAtomContainer) = {
    val smiles: Option[MetaData] = compound.getMetaData.asScala.find(_.getName.toLowerCase == CommonMetaData.SMILES.toLowerCase)

    // Parse SMILES
    if (smiles.isDefined && !smiles.get.getValue.toString.isEmpty) {
      logger.info(s"$id: Converting SMILES to MOL definition")

      val molecule: IAtomContainer = compoundConversion.smilesToMolecule(smiles.get.getValue.toString)

      if (molecule != null) {
        logger.info(s"$id: Generating MOL definition from molecule")
        (compoundConversion.generateMolDefinition(molecule), molecule)
      } else {
        logger.info(s"$id: SMILES conversion failed")

        if (impacts != null) {
          impacts.append(new Impacts(-1, "SMILES conversion failed"))
        }

        (null, null)
      }
    } else {
      logger.info(s"$id: No SMILES found")
      (null, null)
    }
  }
}


@Component
class CompoundInChIKeyProcessor extends AbstractCompoundProcessor {

  // CTS-Lite cannot return a MOL, but it can translate an InChIKey to an InChI or SMILES
  // which we then convert to a structure locally with the CDK
  @Autowired
  protected val ctsLiteService: CTSLiteService = null

  def process(compound: Compound, id: String, impacts: ArrayBuffer[Impacts]): (String, IAtomContainer) = {
    val inchikey: String =
      if (compound.getInchiKey != null)
        compound.getInchiKey
      else
        compound.getMetaData.asScala.filter(_.getName.toLowerCase == CommonMetaData.INCHI_KEY.toLowerCase).map(_.getValue.toString).headOption.orNull

    ctsLiteService.matchInChIKey(inchikey, id) match {
      case Some(structure) =>
        // Prefer the InChI, falling back to the SMILES, to build the molecule locally
        val fromInchi: IAtomContainer =
          if (structure.inchi != null && structure.inchi.nonEmpty) compoundConversion.inchiToMolecule(structure.inchi) else null

        val molecule: IAtomContainer =
          if (fromInchi != null) fromInchi
          else if (structure.smiles != null && structure.smiles.nonEmpty) compoundConversion.smilesToMolecule(structure.smiles)
          else null

        if (molecule != null) {
          logger.info(s"$id: Resolved structure from InChIKey lookup")
          (compoundConversion.generateMolDefinition(molecule), molecule)
        } else {
          logger.info(s"$id: InChIKey lookup returned a match but no usable structure")
          (null, null)
        }

      case None =>
        (null, null)
    }
  }
}
