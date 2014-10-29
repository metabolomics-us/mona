package util.chemical

import org.apache.log4j.Logger
import org.openscience.cdk.Atom
import org.openscience.cdk.Bond
import org.openscience.cdk.Molecule
import org.openscience.cdk.interfaces.IBond
import org.openscience.cdk.io.MDLV2000Writer
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester
import org.openscience.cdk.isomorphism.mcss.RMap
import org.openscience.cdk.layout.StructureDiagramGenerator
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator

/**
 * a simple tool to derivatize a compounds
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 10/15/14
 * Time: 12:29 PM
 */
class Derivatizer {

    Logger logger = Logger.getLogger(getClass())

    /**
     * calculates all possible derivatizations with TMS for the given molecule and returns this in an ordered list
     * @param molecule
     * @return
     */
    List<Molecule> derivatizeWithTMS(Molecule myStructure, List<Molecule> functionalGroups, boolean ignoreImpossibleCompounds = false) {

        Molecule structure = myStructure.clone()

        def result = []

        //we just skip these
        if (ignoreImpossibleCompounds) {

            logger.debug("calculate impossible compounds")
            //all hyrdroxy groups are derivatized at once
            def tempData = derivatizeWithTMS(structure, [FunctionalGroupBuilder.makeHydroxyGroup()])

            if (!tempData.isEmpty()) {
                //assign our hyrdroxy lized compound as the new structe
                structure = tempData.last().clone()

                //add it to the result set
                result.add(structure.clone())
            }
        }

        //calculate all our possible reactions
        functionalGroups.eachWithIndex { Molecule subGroup, int index ->

            int tries = 0


            logger.info("cehcking functional group: ${index}")
            //keep running as long till all substructures are replaced
            while (tries < 100 && UniversalIsomorphismTester.isSubgraph(structure, subGroup)) {
                tries++

                logger.debug("searching for all sub graphs attempt $tries for sub group: $index")
                //find the first connectors for this sub graph
                UniversalIsomorphismTester.getSubgraphMap(structure, subGroup).each { RMap map ->

                    logger.info("\tstructure atom ${map.id1} is connected to substructure atom ${map.id2}")

                    logger.info(structure.getAtomCount())

                    //get the atom which is the functional group
                    Bond bond = structure.getBond(map.id1)

                    Atom atom = bond.getAtom(map.id2)

                    logger.info("\tconnection is at atom ${atom.getSymbol()} ")

                    List connectedAtoms = structure.getConnectedAtomsList(atom)

                    //remove first hydrogen and replace it with TMS
                    for (Atom con : connectedAtoms) {
                        if (con.getSymbol() == "H") {

                            atom.setImplicitHydrogenCount(atom.getImplicitHydrogenCount() - 1)

                            //add our TMS
                            Molecule tms = AdductBuilder.makeTMS()

                            structure.removeBond(structure.getBond(con, atom))
                            structure.removeAtom(con)

                            //add our TMS
                            structure.add(tms)

                            //connect the tms
                            structure.addBond(new Bond(atom, tms.getAtom(0), IBond.Order.SINGLE))

                            StructureDiagramGenerator sdg = new StructureDiagramGenerator();
                            sdg.setMolecule(structure.clone());
                            sdg.generateCoordinates();
                            result.add(sdg.getMolecule() )

                            break;
                        }
                    }

                }
            }
        }

        return result


    }

    /**
     * returns if the molecule is derivatized
     * @param molecule
     * @return
     */
    boolean isDerivatized(Molecule molecule){
        return UniversalIsomorphismTester.isSubgraph(molecule,AdductBuilder.makeTMS())
    }

    /**
     * tries to create the best possible TMS structure for the provided input parameters
     * @param myStructure
     * @param maxTMSCount
     * @param functionalGroups
     * @return
     */
    Molecule generateTMSDerivatizationProduct(Molecule myStructure, int maxTMSCount, List<Molecule> functionalGroups = TMSFavoredFunctionalGroups.buildFavoredGroupsInOrder()) {

        logger.debug("generate most likely derivatization product for $maxTMSCount TMS")

        //generate all products
        List<Molecule> result = derivatizeWithTMS(myStructure, functionalGroups, true)


        return result.last()

    }

    static String getMOLFile(Molecule molecule) {

        molecule =AtomContainerManipulator.removeHydrogens(molecule)

        StructureDiagramGenerator sdg = new StructureDiagramGenerator();
        sdg.setMolecule(molecule);
        sdg.generateCoordinates();
        Molecule layedOutMol = sdg.getMolecule();

        //generate the mol file
        StringWriter writer = new StringWriter()

        //generate our molfile
        MDLV2000Writer mdl = new MDLV2000Writer(writer)
        mdl.write(layedOutMol)
        mdl.close()

        return writer.toString()
    }
}
