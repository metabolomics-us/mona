package edu.ucdavis.fiehnlab.mona.backend.curation.util.chemical

import org.openscience.cdk.interfaces.{IBond, IAtomContainer}
import org.openscience.cdk.smiles.SmilesParser
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator
import org.openscience.cdk.{Bond, DefaultChemObjectBuilder, Atom, AtomContainer}

/**
  * Created by sajjan on 4/1/16.
  */
object FunctionalGroupBuilder {
  /**
    *
    * @param smiles
    * @return
    */
  def parseSmiles(smiles: String): IAtomContainer = {
    new SmilesParser(DefaultChemObjectBuilder.getInstance()).parseSmiles(smiles)
  }


  /**
    *
    * @param molecule
    */
  def makeHydrogens(molecule: IAtomContainer): Unit = {
    AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molecule)
    AtomContainerManipulator.convertImplicitToExplicitHydrogens(molecule)
  }


  /**
    * Builds a hydroxyl group
    *
    * @return
    */
  def makeHydroxyGroup(): IAtomContainer = {
    val molecule: IAtomContainer = new AtomContainer()

    val oxygen: Atom = new Atom("O")
    oxygen.setImplicitHydrogenCount(1)
    molecule.addAtom(oxygen)

    makeHydrogens(molecule)
    molecule
  }

  /**
    * Builds a thiol
    *
    * @return
    */
  def makeThiol(): IAtomContainer = {
    val molecule: IAtomContainer = new AtomContainer()

    val sulfur: Atom = new Atom("S")
    sulfur.setImplicitHydrogenCount(1)
    molecule.addAtom(sulfur)

    makeHydrogens(molecule)
    molecule
  }

  /**
    * Builds a primary amine group
    *
    * @return
    */
  def makePrimaryAmine(): IAtomContainer = {
    val molecule: IAtomContainer = new AtomContainer()

    val nitrogen: Atom = new Atom("N")
    nitrogen.setImplicitHydrogenCount(2)
    molecule.addAtom(nitrogen)

    makeHydrogens(molecule)
    molecule
  }

  /**
    * Builds a secondary amine group
    *
    * @return
    */
  def makeSecondaryAmine(): IAtomContainer = {
    val molecule: IAtomContainer = new AtomContainer()

    val nitrogen: Atom = new Atom("N")
    nitrogen.setImplicitHydrogenCount(1)
    molecule.addAtom(nitrogen)

    makeHydrogens(molecule)
    molecule
  }

  /**
    * Builds a phosphate
    *
    * @return
    */
  def makePhosphate(): IAtomContainer = {
    val molecule: IAtomContainer = new AtomContainer()

    val phosphorus: Atom = new Atom("P")
    phosphorus.setImplicitHydrogenCount(0)
    molecule.addAtom(phosphorus)

    val oxygen1: Atom = new Atom("O")
    val oxygen2: Atom = new Atom("O")

    val hydroxyl1: IAtomContainer = makeHydroxyGroup()
    val hydroxyl2: IAtomContainer = makeHydroxyGroup()

    molecule.addAtom(phosphorus)
    molecule.addAtom(oxygen1)
    molecule.addAtom(oxygen2)
    molecule.add(hydroxyl1)
    molecule.add(hydroxyl2)

    molecule.addBond(new Bond(phosphorus, oxygen1, IBond.Order.SINGLE))
    molecule.addBond(new Bond(phosphorus, oxygen2, IBond.Order.DOUBLE))
    molecule.addBond(new Bond(phosphorus, hydroxyl1.getAtom(0), IBond.Order.SINGLE))
    molecule.addBond(new Bond(phosphorus, hydroxyl2.getAtom(0), IBond.Order.SINGLE))

    makeHydrogens(molecule)
    molecule
  }


  /**
    * Creates a methyl group
    *
    * @return
    */
  def makeMethyl(): IAtomContainer = {
    val molecule: IAtomContainer = new AtomContainer()

    val carbon: Atom = new Atom("C")
    carbon.setImplicitHydrogenCount(3)
    molecule.addAtom(carbon)

    makeHydrogens(molecule)
    molecule
  }
}