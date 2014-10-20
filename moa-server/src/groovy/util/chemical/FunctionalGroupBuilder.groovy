package util.chemical
import org.openscience.cdk.Atom
import org.openscience.cdk.Bond
import org.openscience.cdk.DefaultChemObjectBuilder
import org.openscience.cdk.Molecule
import org.openscience.cdk.interfaces.IBond
import org.openscience.cdk.smiles.SmilesParser
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator
/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 10/14/14
 * Time: 2:32 PM
 */
class FunctionalGroupBuilder {

    static Molecule parseSmile(String smile) {

        return (Molecule) new SmilesParser(DefaultChemObjectBuilder.getInstance()).parseSmiles(smile)
    }

    static void makeHydrogens(Molecule molecule){

        AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molecule);
        AtomContainerManipulator.convertImplicitToExplicitHydrogens(molecule);

    }
    /**
     * builds a hydroxy group
     * @return
     */
    static Molecule makeHydroxyGroup() {

        Molecule molecule = new Molecule()
        Atom atom = new Atom("O")
        atom.setImplicitHydrogenCount(1)
        molecule.addAtom(atom)

        makeHydrogens(molecule)
        return molecule

    }

    static Molecule makeThiol() {
        Molecule molecule = new Molecule()
        Atom atom = new Atom("S")
        atom.setImplicitHydrogenCount(1)

        molecule.addAtom(atom)
        makeHydrogens(molecule)

        return molecule
    }

    static Molecule makePrimaryAmine() {
        Molecule molecule = new Molecule()
        Atom atom = new Atom("N")
        atom.setImplicitHydrogenCount(2)

        molecule.addAtom(atom)

        makeHydrogens(molecule)
        return molecule
    }


    static Molecule makeSecondaeryAmine() {
        Molecule molecule = new Molecule()
        Atom atom = new Atom("N")
        atom.setImplicitHydrogenCount(1)

        molecule.addAtom(atom)
        makeHydrogens(molecule)

        return molecule
    }


    static Molecule makePhosphate() {
        Molecule molecule = new Molecule()

        Atom phosphor = new Atom("P")
        phosphor.setImplicitHydrogenCount(0)
        Atom o1 = new Atom("O")
        Atom o2 = new Atom("O")

        Molecule oh1 = makeHydroxyGroup()
        Molecule oh2 = makeHydroxyGroup()

        molecule.addAtom(phosphor)
        molecule.addAtom(o1)
        molecule.addAtom(o2)
        molecule.add(oh1)
        molecule.add(oh2)

        molecule.addBond(new Bond(phosphor,o1,IBond.Order.SINGLE))
        molecule.addBond(new Bond(phosphor,o2,IBond.Order.DOUBLE))
        molecule.addBond(new Bond(phosphor,oh1.getAtom(0),IBond.Order.SINGLE))
        molecule.addBond(new Bond(phosphor,oh2.getAtom(0),IBond.Order.SINGLE))

        makeHydrogens(molecule)

        return molecule
    }

}
