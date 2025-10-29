package edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound


import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Compound, Impacts, MetaData, Score, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Tag
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound.cts.FetchCTSCompoundData
import edu.ucdavis.fiehnlab.mona.backend.curation.util.CommonMetaData
import org.openscience.cdk.interfaces.IAtomContainer
import org.springframework.batch.item.ItemProcessor
import org.springframework.beans.factory.annotation.Autowired

import scala.collection.mutable.{ArrayBuffer, Buffer}
import scala.jdk.CollectionConverters._

/**
  * Created by sajjan on 4/4/16.
  */
@Step(description = "this step calculates the compound properties using the CDK", previousClass = classOf[FetchCTSCompoundData], workflow = "spectra-curation")
class CalculateCompoundProperties extends ItemProcessor[Spectrum, Spectrum] with LazyLogging {

  @Autowired
  val compoundProcessor: CompoundProcessor = null

  @Autowired
  val compoundConversion: CompoundConversion = null


  override def process(spectrum: Spectrum): Spectrum = {
    logger.info(s"${spectrum.getId}: Calculating compound properties...")

    val impacts: ArrayBuffer[Impacts] = ArrayBuffer[Impacts]()
    val updatedCompound: Buffer[Compound] = spectrum.getCompound.asScala.map(compound => calculateCompoundProperties(compound, spectrum.getId, impacts))

    // Assembled spectrum with updated compounds and scores
    val score = {
      if (spectrum.getScore == null) {
        new Score(impacts.asJava, 0.0, 0.0, 0.0)
      } else {
        impacts.appendAll(spectrum.getScore.getImpacts.asScala)
        spectrum.getScore.setImpacts(impacts.asJava)

        spectrum.getScore
      }
    }
    spectrum.setScore(score)
    spectrum.setCompound(updatedCompound.asJava)
    spectrum

  }


  def calculateCompoundProperties(compound: Compound, id: String, impacts: ArrayBuffer[Impacts]): Compound = {
    logger.info(s"$id: Processing compound: ${compound.getKind}")

    // Updated metadata to add to this compound
    val metaData: ArrayBuffer[MetaData] = new ArrayBuffer[MetaData]()
    compound.getMetaData.asScala.foreach(x => metaData.append(x))


    // Add submitted InChI and InChIKey to metadata if we haven't already
    if (compound.getInchi != null && !compound.getInchi.isEmpty && compound.getMetaData.asScala.forall(_.getName.toLowerCase != CommonMetaData.INCHI_CODE.toLowerCase)) {
      metaData.append(new MetaData(null, CommonMetaData.INCHI_CODE, compound.getInchi, false, "none", false, null))
    }

    if (compound.getInchiKey != null && !compound.getInchiKey.isEmpty && compound.getMetaData.asScala.forall(_.getName.toLowerCase != CommonMetaData.INCHI_KEY.toLowerCase)) {
      metaData.append(new MetaData(null, CommonMetaData.INCHI_KEY, compound.getInchiKey, false, "none", false, null))
    }


    // Get the MOL definition and CDK molecule
    val (molDefinition, molecule): (String, IAtomContainer) = compoundProcessor.process(compound, id)


    if (molecule == null) {
      logger.warn(s"$id: Unable to load provided structure information with CDK")
      impacts.append(new Impacts(-10, "Unable to generate a molecular structure from provided compound data"))
      compound
    } else if (molDefinition == null) {
      logger.warn(s"$id: No MOL definition found")
      impacts.append(new Impacts(-2, "Unable to read or generate MOL data"))
      compound
    } else {
      // Read MOL data
      logger.debug(s"$id: Received mol:\n $molDefinition")

      // Calculate molecular properties
      metaData.append(new MetaData(null, CommonMetaData.MOLECULAR_FORMULA, compoundConversion.moleculeToMolecularFormula(molecule), false, "computed", true, null))

      metaData.append(new MetaData(null, CommonMetaData.TOTAL_EXACT_MASS, compoundConversion.moleculeToTotalExactMass(molecule).toString, false, "computed", true, null))

      // Calculate SMILES
      metaData.append(new MetaData(null, CommonMetaData.SMILES, compoundConversion.moleculeToSMILES(molecule), false, "computed", true, null))


      // Calculate InChI and InChIKey and only add them to the record if they differ from provided values
      val computedInChI: String = compoundConversion.moleculeToInChI(molecule)
      val computedInChIKey: String = compoundConversion.moleculeToInChIKey(molecule)

      val providedInChI: Option[MetaData] = (compound.getMetaData.asScala ++ metaData)
        .find(x => x.getName.toLowerCase == CommonMetaData.INCHI_CODE.toLowerCase && !x.getComputed)
      val providedInChIKey: Option[MetaData] = (compound.getMetaData.asScala ++ metaData)
        .find(x => x.getName.toLowerCase == CommonMetaData.INCHI_KEY.toLowerCase && !x.getComputed)

      // Add computed values if they are not null
      if (computedInChI != null && computedInChIKey != null) {
        if (providedInChI.isEmpty || providedInChI.get.getValue.toString != computedInChI) {
          metaData.append(new MetaData(null, CommonMetaData.INCHI_CODE, computedInChI, false, "computed", true, null))
        }

        if (providedInChIKey.isEmpty || providedInChIKey.get.getValue.toString != computedInChIKey) {
          metaData.append(new MetaData(null, CommonMetaData.INCHI_KEY, computedInChIKey, false, "computed", true, null))
        }

        // Check whether computed InChIKey matches the one given
        if (providedInChIKey.isDefined && providedInChIKey.get.getValue.toString.split('-')(0) != computedInChIKey.split('-')(0)) {
          logger.info(s"$id: Discrepancy between provided and computed InChIKeys (${providedInChIKey.get.getValue}, $computedInChIKey)")
          impacts.append(new Impacts(-1, "Discrepancy between first blocks of the provided and computed InChIKeys"))
        }
      } else {
        logger.warn(s"$id: Could not compute InChI or InChIKey for molecule, skipping comparison")
      }

      // Add positive score impact
      impacts.append(new Impacts(2, s"Valid molecular structure(s) provided for ${compound.getKind} compound"))

      // Return compound with update metadata
      compound.setMolFile(molDefinition)
      compound.setMetaData(metaData.asJava)
      compound
    }
  }
}
