package edu.ucdavis.fiehnlab.mona.backend.curation.util.chemical

import java.io.StringWriter

import org.openscience.cdk.{AtomContainer, Bond, Atom}
import org.openscience.cdk.interfaces.{IAtom, IAtomContainer, IBond}
import org.openscience.cdk.io.MDLV2000Writer
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester
import org.openscience.cdk.isomorphism.mcss.RMap
import org.openscience.cdk.layout.StructureDiagramGenerator
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer

/**
  * Created by sajjan on 4/25/16
  */
class TMSDerivatizer extends Derivatizer {
  val MAX_DERIVITIZATION_ATTEMPTS: Int = 100


  /**
    * Calculates all possible TMS derivatizations for the given molecule
    *
    * @param molecule
    * @param functionalGroups
    * @param ignoreImpossibleCompounds
    */
  def derivatize(molecule: IAtomContainer, functionalGroups: List[IAtomContainer], ignoreImpossibleCompounds: Boolean = false): List[IAtomContainer] = {
    val derivatizations: ListBuffer[IAtomContainer] = ListBuffer()

    // Skip impossible compounds
    if (ignoreImpossibleCompounds) {
      // All hydroxy groups are derivatized at once
      def hydroxyDerivatized: List[IAtomContainer] = derivatize(new AtomContainer(molecule), List(FunctionalGroupBuilder.makeHydroxyGroup()))

      if (hydroxyDerivatized.nonEmpty) {
        // Add our hyrdroxylized compound to the results list
        derivatizations += new AtomContainer(hydroxyDerivatized.last)
      }
    }


    // Assign our structure to the hyrdroxylized compound if defined, otherwise the provided structure
    val structure: IAtomContainer =
      if (derivatizations.nonEmpty) new AtomContainer(derivatizations.last)
      else new AtomContainer(molecule)


    // Universal Isomorphism Tester
    val isomorphismTester: UniversalIsomorphismTester = new UniversalIsomorphismTester()


    // Calculate all possible reactions
    functionalGroups.zipWithIndex.foreach { case (subGroup: IAtomContainer, i: Int) =>
      // Iterate while derivatizations are possible
      (1 to MAX_DERIVITIZATION_ATTEMPTS).foreach { _ =>

        // Search all subgraphs for derivatization subgroup
        isomorphismTester.getSubgraphMap(structure, subGroup).foreach { x: RMap =>
          // Get associated atom and bond
          val bond: IBond = structure.getBond(x.getId1)
          val atom: IAtom = structure.getAtom(x.getId2)

          val connectedAtoms: List[IAtom] = structure.getConnectedAtomsList(atom).toList
          val connectedHydrogens: List[IAtom] = connectedAtoms.filter(_.getSymbol == "H")

          if (connectedHydrogens.nonEmpty) {
            // Get connected hydrogen
            val connectedAtom: IAtom = connectedHydrogens.head

            // Remove a hydrogen from associated atom
            atom.setImplicitHydrogenCount(atom.getImplicitHydrogenCount - 1)

            // Add our TMS group
            val tms: IAtomContainer = AdductBuilder.makeTMS()

            structure.removeBond(structure.getBond(connectedAtom, atom))
            structure.removeAtom(connectedAtom)

            structure.add(tms)
            structure.addBond(new Bond(atom, tms.getAtom(0), IBond.Order.SINGLE))

            // Add molecule with generated coordinates to results list
            val sdg: StructureDiagramGenerator = new StructureDiagramGenerator()
            sdg.setMolecule(new AtomContainer(structure))
            sdg.generateCoordinates()
            derivatizations.add(sdg.getMolecule)
          }
        }
      }
    }

    derivatizations.toList
  }

  /**
    * Checks whether the molecule is derivatized
    *
    * @param molecule
    * @return
    */
  def isDerivatized(molecule: IAtomContainer): Boolean = {
    new UniversalIsomorphismTester().isSubgraph(molecule, AdductBuilder.makeTMS())
  }

  /**
    * Tries to create the best possible TMS structure for the provided molecule and functional groups
    *
    * @param molecule
    * @param maxTMSCount
    * @param functionalGroups
    * @return
    */
  def generateDerivatizationProduct(molecule: IAtomContainer, maxTMSCount: Integer,
                                       functionalGroups: List[IAtomContainer] = TMSFavoredFunctionalGroups.buildFavoredGroupsInOrder()): IAtomContainer = {

    val result: List[IAtomContainer] = derivatize(molecule, functionalGroups, true)

    if (result.nonEmpty) {
      result.last
    } else {
      null
    }
  }

  def getMOLFile(molecule: IAtomContainer): String = {
    // Create structure diagram generator using the hydrogen-stripped version of the given molecule
    val sdg: StructureDiagramGenerator = new StructureDiagramGenerator()
    sdg.setMolecule(AtomContainerManipulator.removeHydrogens(molecule))
    sdg.generateCoordinates()

    // Generate the MOL data
    val writer: StringWriter = new StringWriter()

    val mdl: MDLV2000Writer = new MDLV2000Writer(writer)
    mdl.write(sdg.getMolecule)
    mdl.close()

    writer.toString
  }
}