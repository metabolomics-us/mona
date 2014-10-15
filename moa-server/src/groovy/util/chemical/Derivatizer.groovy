package util.chemical
import org.apache.log4j.Logger
import org.openscience.cdk.Atom
import org.openscience.cdk.Bond
import org.openscience.cdk.Molecule
import org.openscience.cdk.interfaces.IBond
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester
import org.openscience.cdk.isomorphism.mcss.RMap
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
    List<Molecule> derivatizeWithTMS(Molecule myStructure, List<Molecule> functionalGroups){

        Molecule structure = myStructure.clone()

        def result = []

        functionalGroups.each { Molecule subGroup ->

            //keep running as long till all substructures are replaced
            while (UniversalIsomorphismTester.isSubgraph(structure, subGroup)) {

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

                            structure.removeBond(structure.getBond(con, atom))
                            structure.removeAtom(con)
                            //add our TMS
                            Molecule tms = AdductBuilder.makeTMS()

                            //add our TMS
                            structure.add(tms)

                            //connect the tms
                            structure.addBond(new Bond(atom, tms.getAtom(0), IBond.Order.SINGLE))

                            result.add(structure.clone())
                            break;
                        }
                    }

                }
            }
        }

        return result



    }
}
