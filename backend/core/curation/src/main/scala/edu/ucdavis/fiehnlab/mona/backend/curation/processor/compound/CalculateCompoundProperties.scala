package edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound


import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain._
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound.cts.FetchCTSCompoundData
import edu.ucdavis.fiehnlab.mona.backend.curation.util.CommonMetaData
import org.openscience.cdk.interfaces.IAtomContainer
import org.springframework.batch.item.ItemProcessor
import org.springframework.beans.factory.annotation.Autowired

import scala.collection.mutable.ArrayBuffer

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
    logger.info(s"${spectrum.id}: Calculating compound properties...")

    val impacts: ArrayBuffer[Impact] = ArrayBuffer[Impact]()
    val updatedCompound: Array[Compound] = spectrum.compound.map(compound => calculateCompoundProperties(compound, spectrum.id, impacts))

    // Assembled spectrum with updated compounds and scores
    spectrum.copy(
      compound = updatedCompound,
      score =
        if (spectrum.score == null)
          Score(impacts.toArray, 0)
        else
          spectrum.score.copy(impacts = spectrum.score.impacts ++ impacts)
    )
  }


  def calculateCompoundProperties(compound: Compound, id: String, impacts: ArrayBuffer[Impact]): Compound = {
    // Updated metadata to add to this compound
    val metaData: ArrayBuffer[MetaData] = new ArrayBuffer[MetaData]()
    compound.metaData.foreach(x => metaData.append(x))


    // Add submitted InChI and InChIKey to metadata if we haven't already
    if (compound.inchi != null && compound.inchi != "" && compound.metaData.forall(_.name.toLowerCase != CommonMetaData.INCHI_CODE.toLowerCase)) {
      metaData.append(MetaData("none", computed = false, hidden = false, CommonMetaData.INCHI_CODE,
        null, null, null, compound.inchi))
    }

    if (compound.inchiKey != null && compound.inchiKey != "" && compound.metaData.forall(_.name.toLowerCase != CommonMetaData.INCHI_KEY.toLowerCase)) {
      metaData.append(MetaData("none", computed = false, hidden = false, CommonMetaData.INCHI_KEY,
        null, null, null, compound.inchiKey))
    }


    // Get the MOL definition and CDK molecule
    val (molDefinition, molecule): (String, IAtomContainer) = compoundProcessor.process(compound, id)


    if (molDefinition == null) {
      logger.warn(s"$id: No MOL definition found!")
      impacts.append(Impact(-1, "Unable to read or generate MOL data"))
      compound
    }

    else if (molecule == null) {
      logger.warn(s"$id: Unable to load provided structure information with CDK")
      impacts.append(Impact(-1, "Unable to generate a molecular structure from provided compound data"))
      compound
    }

    else {
      // Read MOL data
      logger.debug(s"$id: Received mol:\n $molDefinition")

      // Calculate molecular properties
      metaData.append(MetaData("computed", computed = true, hidden = false, CommonMetaData.MOLECULAR_FORMULA,
        null, null, null, compoundConversion.moleculeToMolecularFormula(molecule)))

      metaData.append(MetaData("computed", computed = true, hidden = false, CommonMetaData.TOTAL_EXACT_MASS,
        null, null, null, compoundConversion.moleculeToTotalExactMass(molecule)))

      // Calculate InChI
      metaData.append(MetaData("computed", computed = true, hidden = false, CommonMetaData.INCHI_CODE,
        null, null, null, compoundConversion.moleculeToInChI(molecule)))

      val computedInChIKey: String = compoundConversion.moleculeToInChIKey(molecule)

      metaData.append(MetaData("computed", computed = true, hidden = false, CommonMetaData.INCHI_KEY,
        null, null, null, computedInChIKey))

      // Calculate SMILES
      metaData.append(MetaData("computed", computed = true, hidden = false, CommonMetaData.SMILES,
        null, null, null, compoundConversion.moleculeToSMILES(molecule)))


      // Add positive score impact
      impacts.append(Impact(1, "Valid molecular structure(s) provided"))

      // Check whether computed InChIKey matches the one given
      val providedInChIKey: Option[MetaData] = compound.metaData
        .find(x => x.name.toLowerCase == CommonMetaData.INCHI_KEY.toLowerCase && ! x.computed)

      if (providedInChIKey.isDefined && providedInChIKey.get.value.toString.split('-')(0) != computedInChIKey.split('-')(0)) {
        logger.info(s"$id: Discrepancy between provided and computed InChIKeys (${providedInChIKey.get.value}, $computedInChIKey)")
        impacts.append(Impact(-1, "Discrepancy between first blocks of the provided and computed InChIKeys"))
      }

      // Return compound with update metadata
      compound.copy(
        molFile = molDefinition,
        metaData = metaData.toArray
      )
    }
  }
}
