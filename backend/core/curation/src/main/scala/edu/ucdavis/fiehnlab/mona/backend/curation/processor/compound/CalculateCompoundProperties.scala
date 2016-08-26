package edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound

import java.io.{StringReader, StringWriter}

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Compound, MetaData, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import edu.ucdavis.fiehnlab.mona.backend.curation.processor.RemoveComputedData
import edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound.cts.FetchCTSCompoundData
import edu.ucdavis.fiehnlab.mona.backend.curation.util.CommonMetaData
import net.sf.jniinchi.INCHI_RET
import org.openscience.cdk.inchi.{InChIGenerator, InChIGeneratorFactory, InChIToStructure}
import org.openscience.cdk.smiles.SmilesGenerator
import org.openscience.cdk.{AtomContainer, DefaultChemObjectBuilder}
import org.openscience.cdk.interfaces.{IAtomContainer, IChemObject, IMolecularFormula}
import org.openscience.cdk.io.{MDLV2000Reader, MDLV2000Writer}
import org.openscience.cdk.tools.CDKHydrogenAdder
import org.openscience.cdk.tools.manipulator.{AtomContainerManipulator, MolecularFormulaManipulator}
import org.springframework.batch.item.ItemProcessor

import scala.collection.mutable.ArrayBuffer

/**
  * Created by sajjan on 4/4/16.
  */
@Step(description = "this step calculates the compound properties using the CDK", previousClass = classOf[FetchCTSCompoundData], workflow = "spectra-curation")
class CalculateCompoundProperties extends ItemProcessor[Spectrum, Spectrum] with LazyLogging {
  override def process(spectrum: Spectrum): Spectrum = {
    val updatedCompound: Array[Compound] = spectrum.compound.map(calculateCompoundProperties)

    // Assembled spectrum with updated compounds
    spectrum.copy(compound = updatedCompound)
  }

  protected def inchiToMol(inchi: String): String = {
    val inchiGeneratorFactory: InChIGeneratorFactory = InChIGeneratorFactory.getInstance()
    val inChIToStructure: InChIToStructure = inchiGeneratorFactory.getInChIToStructure(inchi, DefaultChemObjectBuilder.getInstance())

    if (inChIToStructure.getReturnStatus != INCHI_RET.OKAY && inChIToStructure.getReturnStatus != INCHI_RET.WARNING) {
      null
    } else {
      val stringWriter: StringWriter = new StringWriter()
      val mdlWriter: MDLV2000Writer = new MDLV2000Writer(stringWriter)

      mdlWriter.writeMolecule(inChIToStructure.getAtomContainer)
      mdlWriter.close()

      stringWriter.toString
    }
  }

  def calculateCompoundProperties(compound: Compound): Compound = {
    // Updated metadata to add to this compound
    val metaData: ArrayBuffer[MetaData] = new ArrayBuffer[MetaData]()

    compound.metaData.foreach(x => metaData.append(x))


    val molFile: String =
      if (compound.molFile != null) compound.molFile
      else if (compound.inchi != null) inchiToMol(compound.inchi)
      else null


    if (molFile == null) {
      compound
    }

    else {
      // Read MOL data
      val reader = new MDLV2000Reader(new StringReader(molFile))
      val molecule: IAtomContainer = reader.read(new AtomContainer())

      logger.debug(s"received mol:\n $molFile")

      // Update molecule
      AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molecule)
      CDKHydrogenAdder.getInstance(DefaultChemObjectBuilder.getInstance()).addImplicitHydrogens(molecule)
      AtomContainerManipulator.convertImplicitToExplicitHydrogens(molecule)


      // Get molecular properties
      val molecularFormula: IMolecularFormula = MolecularFormulaManipulator.getMolecularFormula(molecule)

      metaData.append(MetaData("computed", computed = true, hidden = false, CommonMetaData.MOLECULAR_FORMULA,
        null, null, null, MolecularFormulaManipulator.getString(molecularFormula)))

      metaData.append(MetaData("computed", computed = true, hidden = false, CommonMetaData.TOTAL_EXACT_MASS,
        null, null, null, MolecularFormulaManipulator.getTotalExactMass(molecularFormula)))


      // Calculate InChI
      val inchiGenerator: InChIGenerator = InChIGeneratorFactory.getInstance().getInChIGenerator(molecule)

      metaData.append(MetaData("computed", computed = true, hidden = false, CommonMetaData.INCHI_CODE,
        null, null, null, inchiGenerator.getInchi))

      logger.debug(s"return status is: ${inchiGenerator.getReturnStatus}")
      metaData.append(MetaData("computed", computed = true, hidden = false, CommonMetaData.INCHI_KEY,
        null, null, null, inchiGenerator.getInchiKey))


      // Calculate SMILES
      metaData.append(MetaData("computed", computed = true, hidden = false, CommonMetaData.SMILES,
        null, null, null, SmilesGenerator.unique().create(molecule)))


      // Return compound with update metadata
      compound.copy(
        molFile = molFile,
        metaData = metaData.toArray
      )
    }
  }
}