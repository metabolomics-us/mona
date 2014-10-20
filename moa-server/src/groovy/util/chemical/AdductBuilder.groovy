package util.chemical

import org.openscience.cdk.Atom
import org.openscience.cdk.Bond
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

        carbon.setImplicitHydrogenCount(3)

        FunctionalGroupBuilder.makeHydrogens(molecule)
        return molecule
    }

    /**
     * make TMS
     * @return
     */
    static Molecule makeTMS() {
        Molecule mol = new Molecule()

        Atom silicium = new Atom("Si")
        Molecule m1 = makeMethyl()
        Molecule m2 = makeMethyl()
        Molecule m3 = makeMethyl()

        mol.addAtom(silicium)
        mol.add(m1)
        mol.add(m2)
        mol.add(m3)

        mol.addBond(new Bond(silicium,m1.getAtom(0),IBond.Order.SINGLE))
        mol.addBond(new Bond(silicium,m2.getAtom(0),IBond.Order.SINGLE))
        mol.addBond(new Bond(silicium,m3.getAtom(0),IBond.Order.SINGLE))

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
