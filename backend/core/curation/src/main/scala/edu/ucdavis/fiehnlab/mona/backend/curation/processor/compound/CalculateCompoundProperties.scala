package edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound


import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Compound, MetaData, Spectrum}
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

    val updatedCompound: Array[Compound] = spectrum.compound.map(compound => calculateCompoundProperties(compound, spectrum.id))

    // Assembled spectrum with updated compounds
    spectrum.copy(compound = updatedCompound)
  }


  def calculateCompoundProperties(compound: Compound, id: String): Compound = {
    // Updated metadata to add to this compound
    val metaData: ArrayBuffer[MetaData] = new ArrayBuffer[MetaData]()
    compound.metaData.foreach(x => metaData.append(x))


    // Add submitted InChI and InChIKey to metadata if we haven't already
    if (compound.inchi != null && compound.metaData.forall(_.name != CommonMetaData.INCHI_CODE)) {
      metaData.append(MetaData("none", computed = false, hidden = false, CommonMetaData.INCHI_CODE,
        null, null, null, compound.inchi))
    }

    if (compound.inchiKey != null && compound.metaData.forall(_.name != CommonMetaData.INCHI_KEY)) {
      metaData.append(MetaData("none", computed = false, hidden = false, CommonMetaData.INCHI_KEY,
        null, null, null, compound.inchiKey))
    }


    // Get the MOL definition and CDK molecule
    val (molDefinition, molecule): (String, IAtomContainer) = compoundProcessor.process(compound, id)


    if (molDefinition == null) {
      logger.warn(s"$id: No MOL definition found!")
      compound
    }

    else if (molecule == null) {
      logger.warn(s"$id: Unable to load provided structure information with CDK")
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

      metaData.append(MetaData("computed", computed = true, hidden = false, CommonMetaData.INCHI_KEY,
        null, null, null, compoundConversion.moleculeToInChIKey(molecule)))

      // Calculate SMILES
      metaData.append(MetaData("computed", computed = true, hidden = false, CommonMetaData.SMILES,
        null, null, null, compoundConversion.moleculeToSMILES(molecule)))

      // Return compound with update metadata
      compound.copy(
        molFile = molDefinition,
        metaData = metaData.toArray
      )
    }
  }
}
