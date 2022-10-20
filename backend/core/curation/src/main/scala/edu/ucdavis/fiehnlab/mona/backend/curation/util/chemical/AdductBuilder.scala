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
    "[M+H]+" -> { M: Double => M + 1.00797},
    "[2M+H]+" -> { M: Double => 2 * M + 1.00797 },
    "[M+NH4]+" -> { M: Double => M + 18.03858 },
    "[2M+NH4]+" -> { M: Double => 2 * M +18.03858 },
    "[M+K]+" -> { M: Double => M + 39.0983 },
    "[2M+K]+" -> { M: Double => 2 * M +39.0983 },
    "[M+H-H2O]+" -> { M: Double => M - 17.00737 },
    "[2M+H-H2O]+" -> { M: Double => 2 * M - 17.00737 },
    "[M+Na]+" -> { M: Double => M + 22.98977 },
    "[2M+Na]+" -> { M: Double => 2 * M + 22.98977 }
  )

  /**
    * Definitions of negative mode LC-MS adducts
    * http://fiehnlab.ucdavis.edu/staff/kind/Metabolomics/MS-Adduct-Calculator/
    */
  final val LCMS_NEGATIVE_ADDUCTS: Map[String, Double => Double] = Map(
    "[M-H]-" -> { M: Double => M - 1.007276 },
    "[2M-H]-" -> { M: Double => 2 * M - 1.007276 },
    "[M+Cl]-" -> { M: Double => M + 35.453 },
    "[2M+Cl]-" -> { M: Double => 2 * M + 35.453 },
    "[M+HAc-H]-" -> { M: Double => M + 59.04403 },
    "[2M+HAc-H]-" -> { M: Double => 2 * M + 59.04403 }
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
