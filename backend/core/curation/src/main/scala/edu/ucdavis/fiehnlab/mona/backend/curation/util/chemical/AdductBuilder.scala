package edu.ucdavis.fiehnlab.mona.backend.curation.util.chemical

import org.openscience.cdk.interfaces.{IBond, IAtomContainer}
import org.openscience.cdk.{Bond, Atom, AtomContainer}

/**
  * Created by sajjan on 4/1/16.
  */
object AdductBuilder {

  /**
    * Definitions of positive mode lcms adducts
    * http://fiehnlab.ucdavis.edu/staff/kind/Metabolomics/MS-Adduct-Calculator/
    */
  final val LCMS_POSITIVE_ADDUCTS: Map[String, (Double) => Double] = Map(
    "[M+3H]+" -> {M: Double => M / 3.0 + 1.007276},
    "[M+2H+Na]+" -> {M: Double => M / 3.0 + 8.334590},
    "[M+H+2Na]+" -> {M: Double => M / 3 + 15.7661904},
    "[M+3Na]+" -> {M: Double => M / 3.0 + 22.989218},
    "[M+2H]+" -> {M: Double => M / 2.0 + 1.007276},
    "[M+H+NH4]+" -> {M: Double => M / 2.0 + 9.520550},
    "[M+H+Na]+" -> {M: Double => M / 2.0 + 11.998247},
    "[M+H+K]+" -> {M: Double => M / 2.0 + 19.985217},
    "[M+ACN+2H]+" -> {M: Double => M / 2.0 + 21.520550},
    "[M+2Na]+" -> {M: Double => M / 2.0 + 22.989218},
    "[M+2ACN+2H]+" -> {M: Double => M / 2.0 + 42.033823},
    "[M+3ACN+2H]+" -> {M: Double => M / 2.0 + 62.547097},
    "[M+H]+" -> {M: Double => M + 1.007276},
    "[M+NH4]+" -> {M: Double => M + 18.033823},
    "[M+Na]+" -> {M: Double => M + 22.989218},
    "[M+CH3OH+H]+" -> {M: Double => M + 33.033489},
    "[M+K]+" -> {M: Double => M + 38.963158},
    "[M+ACN+H]+" -> {M: Double => M + 42.033823},
    "[M+2Na-H]+" -> {M: Double => M + 44.971160},
    "[M+IsoProp+H]+" -> {M: Double => M + 61.06534},
    "[M+ACN+Na]+" -> {M: Double => M + 64.015765},
    "[M+2K-H]+" -> {M: Double => M + 76.919040},
    "[M+DMSO+H]+" -> {M: Double => M + 79.02122},
    "[M+2ACN+H]+" -> {M: Double => M + 83.060370},
    "[M+IsoProp+Na+H]+" -> {M: Double => M + 84.05511},
    "[2M+H]+" -> {M: Double => 2 * M + 1.007276},
    "[2M+NH4]+" -> {M: Double => 2 * M + 18.033823},
    "[2M+Na]+" -> {M: Double => 2 * M + 22.989218},
    "[2M+3H2O+2H]+" -> {M: Double => 2 * M + 28.02312},
    "[2M+K]+" -> {M: Double => 2 * M + 38.963158},
    "[2M+ACN+H]+" -> {M: Double => 2 * M + 42.033823},
    "[2M+ACN+Na]+" -> {M: Double => 2 * M + 64.015765}
  )

  /**
    * Definitions of negative mode lcms adducts
    * http://fiehnlab.ucdavis.edu/staff/kind/Metabolomics/MS-Adduct-Calculator/
    */
  final val LCMS_NEGATIVE_ADDUCTS: Map[String, (Double) => Double] = Map(
    "[M-3H]-" -> {M: Double => M / 3.0 - 1.007276},
    "[M-2H]-" -> {M: Double => M / 2.0 - 1.007276},
    "[M-H2O-H]-" -> {M: Double => M - 19.01839},
    "[M-H]-" -> {M: Double => M - 1.007276},
    "[M+Na-2H]-" -> {M: Double => M + 20.974666},
    "[M+Cl]-" -> {M: Double => M + 34.969402},
    "[M+K-2H]-" -> {M: Double => M + 36.948606},
    "[M+FA-H]-" -> {M: Double => M + 44.998201},
    "[M+Hac-H]-" -> {M: Double => M + 59.013851},
    "[M+Br]-" -> {M: Double => M + 78.918885},
    "[M+TFA-H]-" -> {M: Double => M + 112.985586},
    "[2M-H]-" -> {M: Double => 2 * M - 1.007276},
    "[2M+FA-H]-" -> {M: Double => 2 * M + 44.998201},
    "[2M+Hac-H]-" -> {M: Double => 2 * M + 59.013851},
    "[3M-H]-" -> {M: Double => 3 * M - 1.007276},
    "[M+CH3OH+H]-" -> {M: Double => M + 33.033489},
    "[M+K]-" -> {M: Double => M + 38.963158},
    "[M+ACN+H]-" -> {M: Double => M + 42.033823},
    "[M+2Na-H]-" -> {M: Double => M + 44.971160},
    "[M+IsoProp+H]-" -> {M: Double => M + 61.06534},
    "[M+ACN+Na]-" -> {M: Double => M + 64.015765},
    "[M+2K-H]-" -> {M: Double => M + 76.919040},
    "[M+DMSO+H]-" -> {M: Double => M + 79.02122},
    "[M+2ACN+H]-" -> {M: Double => M + 83.060370},
    "[M+IsoProp+Na+H]-" -> {M: Double => M + 84.05511},
    "[2M+H]-" -> {M: Double => 2 * M + 1.007276},
    "[2M+NH4]-" -> {M: Double => 2 * M + 18.033823},
    "[2M+Na]-" -> {M: Double => 2 * M + 22.989218},
    "[2M+3H2O+2H]-" -> {M: Double => 2 * M + 28.02312},
    "[2M+K]-" -> {M: Double => 2 * M + 38.963158},
    "[2M+ACN+H]-" -> {M: Double => 2 * M + 42.033823},
    "[2M+ACN+Na]-" -> {M: Double => 2 * M + 64.015765}
  )


  /**
    * Make TMS group
    * @return
    */
  def makeTMS(): IAtomContainer = {
    val molecule: IAtomContainer = new AtomContainer()

    val silicon: Atom = new Atom("Si")
    val methyl1: IAtomContainer = FunctionalGroupBuilder.makeMethyl()
    val methyl2: IAtomContainer = FunctionalGroupBuilder.makeMethyl()
    val methyl3: IAtomContainer = FunctionalGroupBuilder.makeMethyl()

    molecule.addAtom(silicon)
    molecule.add(methyl1)
    molecule.add(methyl2)
    molecule.add(methyl3)

    molecule.addBond(new Bond(silicon, methyl1.getAtom(0), IBond.Order.SINGLE))
    molecule.addBond(new Bond(silicon, methyl2.getAtom(0), IBond.Order.SINGLE))
    molecule.addBond(new Bond(silicon, methyl3.getAtom(0), IBond.Order.SINGLE))

    FunctionalGroupBuilder.makeHydrogens(molecule)
    molecule
  }

  /**
    * Make a TMS group with a bound to oxygen
    * @return
    */
  def makeTMSBoundToOxygen(): IAtomContainer = {
    val molecule: IAtomContainer = makeTMS()

    molecule.addAtom(new Atom("O"))
    molecule.addBond(0, 4, IBond.Order.SINGLE)

    molecule
  }

  /**
    * Make a TMS group with a bound to nitrogen
    * @return
    */
  def makeTMSBoundToNitrogen(): IAtomContainer = {
    val molecule: IAtomContainer = makeTMS()

    molecule.addAtom(new Atom("N"))
    molecule.addBond(0, 4, IBond.Order.SINGLE)

    molecule
  }

  /**
    * Make a TMS group with a bound to sulfur
    * @return
    */
  def makeTMSBoundToSulfur(): IAtomContainer = {
    val molecule: IAtomContainer = makeTMS()

    molecule.addAtom(new Atom("S"))
    molecule.addBond(0, 4, IBond.Order.SINGLE)

    molecule
  }

  /**
    * Make a TMS group with a bound to phosphorus
    * @return
    */
  def makeTMSBoundToPhosphorus(): IAtomContainer = {
    val molecule: IAtomContainer = makeTMS()

    molecule.addAtom(new Atom("P"))
    molecule.addBond(0, 4, IBond.Order.SINGLE)

    molecule
  }
}
