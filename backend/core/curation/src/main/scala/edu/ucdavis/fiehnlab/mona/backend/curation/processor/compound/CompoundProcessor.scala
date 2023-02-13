package edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Compound, Impacts, MetaData}
import edu.ucdavis.fiehnlab.mona.backend.curation.util.CommonMetaData
import org.openscience.cdk.interfaces.IAtomContainer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.stereotype.{Component, Service}
import org.springframework.web.client.RestOperations

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

    val molProcessorResult =
      try {
        molProcessor.process(compound, id, impacts)
      } catch {
        case e: Exception =>
          e.printStackTrace()
          null
      }

    val inchiProcessorResult =
      try {
        inchiProcessor.process(compound, id, impacts)
      } catch {
        case e: Exception =>
          e.printStackTrace()
          null
      }

    val smilesProcessorResult =
      try {
        smilesProcessor.process(compound, id, impacts)
      } catch {
        case e: Exception =>
          e.printStackTrace()
          null
      }

    val inchikeyProcessorResult =
      try {
        inchikeyProcessor.process(compound, id, impacts)
      } catch {
        case e: Exception =>
          e.printStackTrace()
          null
      }

    if (molProcessorResult._1 != null && molProcessorResult._2 != null) {
      logger.info(s"$id: Using provided MOL definition")
      molProcessorResult
    } else if (inchiProcessorResult._1 != null && inchiProcessorResult._2 != null) {
      logger.info(s"$id: Using provided InChI")
      inchiProcessorResult
    } else if (smilesProcessorResult._1 != null && smilesProcessorResult._2 != null) {
      logger.info(s"$id: Using provided SMILES")
      smilesProcessorResult
    } else if (inchikeyProcessorResult._1 != null && inchikeyProcessorResult._2 != null) {
      logger.info(s"$id: Using provided InChIKey")
      inchikeyProcessorResult
    } else {
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
        logger.info(s"$id: Converting SMILES to MOL definition")
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

  val CTS_URL: String = "http://cts.fiehnlab.ucdavis.edu/service/inchikeytomol/"

  @Autowired
  protected val restOperations: RestOperations = null

  def process(compound: Compound, id: String, impacts: ArrayBuffer[Impacts]): (String, IAtomContainer) = {
    val inchikey: String =
      if (compound.getInchiKey != null)
        compound.getInchiKey
      else
        compound.getMetaData.asScala.filter(_.getName.toLowerCase == CommonMetaData.INCHI_KEY.toLowerCase).map(_.getValue.toString).headOption.orNull

    // Lookup InChIKey
    if (inchikey != null && !inchikey.isEmpty) {
      logger.info(s"$id: Looking up MOL definition by InChIKey on CTS, invoking url $CTS_URL$inchikey")

      try {
        val response: ResponseEntity[CTSInChIKeyLookupResponse] = restOperations.getForEntity(CTS_URL + inchikey, classOf[CTSInChIKeyLookupResponse])

        if (response.getStatusCode == HttpStatus.OK) {
          val molDefinition: String = response.getBody.molecule
          val molecule: IAtomContainer = compoundConversion.parseMolDefinition(molDefinition)

          if (molDefinition != null && molDefinition.nonEmpty) {
            logger.info(s"$id: Request successful, parsing MOL definition")
            (compoundConversion.generateMolDefinition(molecule), molecule)
          } else {
            logger.info(s"$id: InChIKey lookup failed, ${response.getBody.message}")
            (null, null)
          }
        } else {
          logger.info(s"$id: InChIKey lookup failed with status code ${response.getStatusCode}")
          (null, null)
        }
      } catch {
        case e: Throwable =>
          logger.error(s"$id: Error during InChIKey lookup: ${e.getMessage}")
          (null, null)
      }
    } else {
      logger.info(s"$id: No InChIKey found")
      (null, null)
    }
  }
}

case class CTSInChIKeyLookupResponse(molecule: String, message: String)
