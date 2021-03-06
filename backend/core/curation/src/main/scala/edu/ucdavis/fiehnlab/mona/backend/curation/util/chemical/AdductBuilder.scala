package edu.ucdavis.fiehnlab.mona.backend.curation.util.chemical

import com.typesafe.scalalogging.LazyLogging
import org.openscience.cdk.interfaces.{IAtomContainer, IBond}
import org.openscience.cdk.{Atom, AtomContainer, Bond}

/**
  * Created by sajjan on 4/1/16.
  */
object AdductBuilder extends LazyLogging {

  private final val E_MASS: Double = 5.4857990946e-4

  /**
    * Definitions of positive mode LC-MS adducts
    * http://fiehnlab.ucdavis.edu/staff/kind/Metabolomics/MS-Adduct-Calculator/
    */
  final val LCMS_POSITIVE_ADDUCTS: Map[String, Double => Double] = Map(
    "[M]+" -> { M: Double => M - E_MASS },
    "[M+3H]+" -> { M: Double => M / 3.0 + 1.007276 },
    "[M+2H+Na]+" -> { M: Double => M / 3.0 + 8.334590 },
    "[M+H+2Na]+" -> { M: Double => M / 3 + 15.7661904 },
    "[M+3Na]+" -> { M: Double => M / 3.0 + 22.989218 },
    "[M+2H]+" -> { M: Double => M / 2.0 + 1.007276 },
    "[M+H+NH4]+" -> { M: Double => M / 2.0 + 9.520550 },
    "[M+H+Na]+" -> { M: Double => M / 2.0 + 11.998247 },
    "[M+H+K]+" -> { M: Double => M / 2.0 + 19.985217 },
    "[M+ACN+2H]+" -> { M: Double => M / 2.0 + 21.520550 },
    "[M+2Na]+" -> { M: Double => M / 2.0 + 22.989218 },
    "[M+2ACN+2H]+" -> { M: Double => M / 2.0 + 42.033823 },
    "[M+3ACN+2H]+" -> { M: Double => M / 2.0 + 62.547097 },
    "[M+H]+" -> { M: Double => M + 1.007276 },
    "[M+NH4]+" -> { M: Double => M + 18.033823 },
    "[M+Na]+" -> { M: Double => M + 22.989218 },
    "[M+CH3OH+H]+" -> { M: Double => M + 33.033489 },
    "[M+K]+" -> { M: Double => M + 38.963158 },
    "[M+ACN+H]+" -> { M: Double => M + 42.033823 },
    "[M+2Na-H]+" -> { M: Double => M + 44.971160 },
    "[M+IsoProp+H]+" -> { M: Double => M + 61.06534 },
    "[M+ACN+Na]+" -> { M: Double => M + 64.015765 },
    "[M+2K-H]+" -> { M: Double => M + 76.919040 },
    "[M+DMSO+H]+" -> { M: Double => M + 79.02122 },
    "[M+2ACN+H]+" -> { M: Double => M + 83.060370 },
    "[M+IsoProp+Na+H]+" -> { M: Double => M + 84.05511 },
    "[2M+H]+" -> { M: Double => 2 * M + 1.007276 },
    "[2M+NH4]+" -> { M: Double => 2 * M + 18.033823 },
    "[2M+Na]+" -> { M: Double => 2 * M + 22.989218 },
    "[2M+3H2O+2H]+" -> { M: Double => 2 * M + 28.02312 },
    "[2M+K]+" -> { M: Double => 2 * M + 38.963158 },
    "[2M+ACN+H]+" -> { M: Double => 2 * M + 42.033823 },
    "[2M+ACN+Na]+" -> { M: Double => 2 * M + 64.015765 },
    "[M-H2O+H]+" -> { M: Double => M - 17.003838 }
  )

  /**
    * Definitions of negative mode LC-MS adducts
    * http://fiehnlab.ucdavis.edu/staff/kind/Metabolomics/MS-Adduct-Calculator/
    */
  final val LCMS_NEGATIVE_ADDUCTS: Map[String, Double => Double] = Map(
    "[M]-" -> { M: Double => M + E_MASS },
    "[M-3H]-" -> { M: Double => M / 3.0 - 1.007276 },
    "[M-2H]-" -> { M: Double => M / 2.0 - 1.007276 },
    "[M-H2O-H]-" -> { M: Double => M - 19.01839 },
    "[M-H]-" -> { M: Double => M - 1.007276 },
    "[M+Na-2H]-" -> { M: Double => M + 20.974666 },
    "[M+Cl]-" -> { M: Double => M + 34.969402 },
    "[M+K-2H]-" -> { M: Double => M + 36.948606 },
    "[M+FA-H]-" -> { M: Double => M + 44.998201 },
    "[M+Hac-H]-" -> { M: Double => M + 59.013851 },
    "[M+Br]-" -> { M: Double => M + 78.918885 },
    "[M+TFA-H]-" -> { M: Double => M + 112.985586 },
    "[2M-H]-" -> { M: Double => 2 * M - 1.007276 },
    "[2M+FA-H]-" -> { M: Double => 2 * M + 44.998201 },
    "[2M+Hac-H]-" -> { M: Double => 2 * M + 59.013851 },
    "[3M-H]-" -> { M: Double => 3 * M - 1.007276 }
  )


  /**
    *
    * @param adduct
    * @return
    */
  def findAdduct(adduct: String): (String, String, Double => Double) = {
    if (adduct == null) {
      (adduct, "not found", null)
    } else {
      // Determine ionization mode from adduct
      val ionizationMode: String = {
        val x: String = adduct.split(']').last

        if (x.length > 1 && x.contains('+')) {
          "positive"
        } else if (x.length > 1 && x.contains('-')) {
          "negative"
        } else {
          null
        }
      }

      // Strip square brackets and trailing +/-/* and split into groups, keeping the +/- signs
      val blocks: Seq[String] = adduct.stripPrefix("[").reverse.dropWhile(c => "+-*]".contains(c)).reverse.split("(?=[+-])")

      // Search lazily for all permutations adduct terms after the first
      blocks.tail
        .permutations
        .map(blocks.head + _.mkString)
        .collectFirst {
          case x if ionizationMode != "negative" && LCMS_POSITIVE_ADDUCTS.contains(s"[$x]+") =>
            logger.info(s"Found adduct match: $adduct -> [$x]+")
            (s"[$x]+", "positive", LCMS_POSITIVE_ADDUCTS(s"[$x]+"))

          case x if ionizationMode != "positive" && LCMS_NEGATIVE_ADDUCTS.contains(s"[$x]-") =>
            logger.info(s"Found adduct match: $adduct -> [$x]-")
            (s"[$x]-", "negative", LCMS_NEGATIVE_ADDUCTS(s"[$x]-"))
        }.getOrElse((adduct, "not found", null))
    }
  }


  /**
    * Make TMS group
    *
    * @return
    */
  def makeTMSGroup(): IAtomContainer = {
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
    *
    * @return
    */
  def makeTMSGroupBoundToOxygen(): IAtomContainer = {
    val molecule: IAtomContainer = makeTMSGroup()

    molecule.addAtom(new Atom("O"))
    molecule.addBond(0, 4, IBond.Order.SINGLE)

    molecule
  }

  /**
    * Make a TMS group with a bound to nitrogen
    *
    * @return
    */
  def makeTMSGroupBoundToNitrogen(): IAtomContainer = {
    val molecule: IAtomContainer = makeTMSGroup()

    molecule.addAtom(new Atom("N"))
    molecule.addBond(0, 4, IBond.Order.SINGLE)

    molecule
  }

  /**
    * Make a TMS group with a bound to sulfur
    *
    * @return
    */
  def makeTMSGroupBoundToSulfur(): IAtomContainer = {
    val molecule: IAtomContainer = makeTMSGroup()

    molecule.addAtom(new Atom("S"))
    molecule.addBond(0, 4, IBond.Order.SINGLE)

    molecule
  }

  /**
    * Make a TMS group with a bound to phosphorus
    *
    * @return
    */
  def makeTMSGroupBoundToPhosphorus(): IAtomContainer = {
    val molecule: IAtomContainer = makeTMSGroup()

    molecule.addAtom(new Atom("P"))
    molecule.addBond(0, 4, IBond.Order.SINGLE)

    molecule
  }
}