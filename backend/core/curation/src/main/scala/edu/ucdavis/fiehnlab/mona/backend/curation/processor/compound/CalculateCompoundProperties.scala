package edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound

import java.io.StringReader

import edu.ucdavis.fiehnlab.mona.backend.core.domain.{MetaData, Compound, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import edu.ucdavis.fiehnlab.mona.backend.curation.processor.RemoveComputedData
import org.openscience.cdk.inchi.{InChIGenerator, InChIGeneratorFactory}
import org.openscience.cdk.smiles.SmilesGenerator
import org.openscience.cdk.{AtomContainer, DefaultChemObjectBuilder}
import org.openscience.cdk.interfaces.{IAtomContainer, IChemObject, IMolecularFormula}
import org.openscience.cdk.io.MDLV2000Reader
import org.openscience.cdk.tools.CDKHydrogenAdder
import org.openscience.cdk.tools.manipulator.{MolecularFormulaManipulator, AtomContainerManipulator}
import org.springframework.batch.item.ItemProcessor

import scala.collection.mutable.ArrayBuffer

/**
  * Created by sajjan on 4/4/16.
  */
@Step(description = "this step calculates the compound properties using the CDK", previousClass = classOf[RemoveComputedData], workflow = "spectra-curation")
class CalculateCompoundProperties extends ItemProcessor[Spectrum, Spectrum] {
  override def process(spectrum: Spectrum): Spectrum = {
    val updatedBiologicalCompound =
      if (spectrum.biologicalCompound != null) calculateCompoundProperties(spectrum.biologicalCompound) else null

    val updatedChemicalCompound =
      if (spectrum.biologicalCompound != null) calculateCompoundProperties(spectrum.chemicalCompound) else null

    // Assembled spectrum with updated compounds
    spectrum.copy(
      biologicalCompound = updatedBiologicalCompound,
      chemicalCompound = updatedChemicalCompound
    )
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

    metaData.append(new MetaData("computed", true, false, "chemical formula",
      null, null, null, MolecularFormulaManipulator.getString(molecularFormula)))

    metaData.append(new MetaData("computed", true, false, "total exact mass",
      null, null, null, MolecularFormulaManipulator.getTotalExactMass(molecularFormula)))


    // Calculate InChI
    val inchiGenerator: InChIGenerator = InChIGeneratorFactory.getInstance().getInChIGenerator(molecule)

    metaData.append(new MetaData("computed", true, false, "InChI",
      null, null, null, inchiGenerator.getInchi))

    metaData.append(new MetaData("computed", true, false, "InChIKey",
      null, null, null, inchiGenerator.getInchiKey))


    // Calculate SMILES
    metaData.append(new MetaData("computed", true, false, "SMILES",
      null, null, null, SmilesGenerator.generic().create(molecule)))


    // Return compound with update metadata
    compound.copy(
      metaData = metaData.toArray
    )
  }
}