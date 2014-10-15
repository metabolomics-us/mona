package util.chemical

import org.openscience.cdk.Atom
import org.openscience.cdk.Molecule
import org.openscience.cdk.interfaces.IBond

/**
 * builds useful adducts for us
 * User: wohlgemuth
 * Date: 10/14/14
 * Time: 1:33 PM
 */
class AdductBuilder {

    /**
     * make methyl
     * @return
     */
    static Molecule makeMethyl() {
        Molecule molecule = new Molecule()
        Atom carbon = new Atom("C")
        molecule.addAtom(carbon)
        molecule.addAtom(new Atom("H"))
        molecule.addAtom(new Atom("H"))
        molecule.addAtom(new Atom("H"))

        molecule.addBond(0,1,IBond.Order.SINGLE)
        molecule.addBond(0,2,IBond.Order.SINGLE)
        molecule.addBond(0,3,IBond.Order.SINGLE)

        return molecule
    }

    /**
     * make TMS
     * @return
     */
    static Molecule makeTMS() {
        Molecule mol = new Molecule()

        mol.addAtom(new Atom("Si"))
        mol.add(makeMethyl())
        mol.add(makeMethyl())
        mol.add(makeMethyl())

        mol.addBond(0, 1, IBond.Order.SINGLE)
        mol.addBond(0, 2, IBond.Order.SINGLE)
        mol.addBond(0, 3, IBond.Order.SINGLE)

        return mol;
    }

    /**
     * build a TMS molecule with a bound to Oxygen
     * @return
     */
    static Molecule makeTMSBoundToOxygen() {
        Molecule mol = makeTMS()

        mol.addAtom(new Atom("O"))
        mol.addBond(0, 4, IBond.Order.SINGLE)

        return mol
    }

    /**
     * build a TMS molecule with a bound to Nitrogen
     * @return
     */
    static Molecule makeTMSBoundToNitrogen() {
        Molecule mol = makeTMS()
        mol.addAtom(new Atom("N"))
        mol.addBond(0, 4, IBond.Order.SINGLE)

        return mol
    }

    /**
     * build a TMS molecule with a bound to Sulfur
     * @return
     */
    static Molecule makeTMSBoundToSulfur() {
        Molecule mol = makeTMS()
        mol.addAtom(new Atom("S"))
        mol.addBond(0, 4, IBond.Order.SINGLE)

        return mol
    }

    /**
     * Build a TMS molecule with a bound to Phosphor
     * @return
     */
    static Molecule makeTMSBoundToPhosphor() {
        Molecule mol = makeTMS()
        mol.addAtom(new Atom("P"))
        mol.addBond(0, 4, IBond.Order.SINGLE)

        return mol
    }


}
