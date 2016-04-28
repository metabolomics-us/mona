package edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound

import java.io.StringReader

import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Compound, MetaData, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import edu.ucdavis.fiehnlab.mona.backend.curation.common.CommonMetaData
import edu.ucdavis.fiehnlab.mona.backend.curation.processor.RemoveComputedData
import edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound.cts.FetchCompoundData
import org.openscience.cdk.inchi.{InChIGenerator, InChIGeneratorFactory}
import org.openscience.cdk.smiles.SmilesGenerator
import org.openscience.cdk.{AtomContainer, DefaultChemObjectBuilder}
import org.openscience.cdk.interfaces.{IAtomContainer, IChemObject, IMolecularFormula}
import org.openscience.cdk.io.MDLV2000Reader
import org.openscience.cdk.tools.CDKHydrogenAdder
import org.openscience.cdk.tools.manipulator.{AtomContainerManipulator, MolecularFormulaManipulator}
import org.springframework.batch.item.ItemProcessor

import scala.collection.mutable.ArrayBuffer

/**
  * Created by sajjan on 4/4/16.
  */
@Step(description = "this step calculates the compound properties using the CDK", previousClass = classOf[FetchCompoundData], workflow = "spectra-curation")
class CalculateCompoundProperties extends ItemProcessor[Spectrum, Spectrum] {
  override def process(spectrum: Spectrum): Spectrum = {
    val updatedCompound: Array[Compound] = spectrum.compound.map(calculateCompoundProperties)

    // Assembled spectrum with updated compounds
    spectrum.copy(compound = updatedCompound)
  }

  def calculateCompoundProperties(compound: Compound): Compound = {
    // Updated metadata to add to this compound
    val metaData: ArrayBuffer[MetaData] = new ArrayBuffer[MetaData]()

    compound.metaData.foreach(x => metaData.append(x))


    // Read MOL data
    val reader = new MDLV2000Reader(new StringReader(compound.molFile))
    val molecule: IAtomContainer = reader.read(new AtomContainer())


    // Update molecule
    AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molecule)
    CDKHydrogenAdder.getInstance(DefaultChemObjectBuilder.getInstance()).addImplicitHydrogens(molecule)
    AtomContainerManipulator.convertImplicitToExplicitHydrogens(molecule)


    // Get molecular properties
    val molecularFormula: IMolecularFormula = MolecularFormulaManipulator.getMolecularFormula(molecule)

    metaData.append(new MetaData("computed", true, false, CommonMetaData.MOLECULAR_FORMULA,
      null, null, null, MolecularFormulaManipulator.getString(molecularFormula)))

    metaData.append(new MetaData("computed", true, false, CommonMetaData.TOTAL_EXACT_MASS,
      null, null, null, MolecularFormulaManipulator.getTotalExactMass(molecularFormula)))


    // Calculate InChI
    val inchiGenerator: InChIGenerator = InChIGeneratorFactory.getInstance().getInChIGenerator(molecule)

    metaData.append(new MetaData("computed", true, false, CommonMetaData.INCHI_CODE,
      null, null, null, inchiGenerator.getInchi))

    metaData.append(new MetaData("computed", true, false, CommonMetaData.INCHI_KEY,
      null, null, null, inchiGenerator.getInchiKey))


    // Calculate SMILES
    metaData.append(new MetaData("computed", true, false, CommonMetaData.SMILES,
      null, null, null, SmilesGenerator.generic().create(molecule)))


    // Return compound with update metadata
    compound.copy(
      metaData = metaData.toArray
    )
  }
}