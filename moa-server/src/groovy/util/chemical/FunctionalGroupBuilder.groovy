package util.chemical

import org.openscience.cdk.Atom
import org.openscience.cdk.Molecule
import org.openscience.cdk.interfaces.IBond

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 10/14/14
 * Time: 2:32 PM
 */
class FunctionalGroupBuilder {

    /**
     * builds a hydroxy group
     * @return
     */
    static Molecule makeHydroxyGroup(){
        Molecule molecule = new Molecule()
        Atom atom = new Atom("O")
        atom.setImplicitHydrogenCount(0)

        molecule.addAtom(new Atom("H"))
        molecule.addAtom(atom)

        molecule.addBond(0,1, IBond.Order.SINGLE)
        return molecule
    }

    static Molecule makeThiol(){
        Molecule molecule = new Molecule()
        Atom atom = new Atom("S")
        atom.setImplicitHydrogenCount(0)

        molecule.addAtom(atom)
        molecule.addAtom(new Atom("H"))

        molecule.addBond(0,1, IBond.Order.SINGLE)

        return molecule
    }

    static Molecule makePrimaryAmine(){
        Molecule molecule = new Molecule()
        Atom atom = new Atom("N")
        atom.setImplicitHydrogenCount(0)

        molecule.addAtom(atom)

        molecule.addAtom(new Atom("H"))
        molecule.addAtom(new Atom("H"))

        molecule.addBond(0,1, IBond.Order.SINGLE)
        molecule.addBond(0,2, IBond.Order.SINGLE)

        return molecule
    }


    static Molecule makeSecondaeryAmine(){
        Molecule molecule = new Molecule()
        Atom atom = new Atom("N")
        atom.setImplicitHydrogenCount(0)

        molecule.addAtom(atom)
        molecule.addAtom(new Atom("H"))
        molecule.addBond(0,1, IBond.Order.SINGLE)

        return molecule
    }


    static Molecule makePhosphate(){
        Molecule molecule = new Molecule()

        molecule.addAtom(new Atom("P"))

        molecule.add(makeHydroxyGroup())
        molecule.add(makeHydroxyGroup())
        molecule.addAtom(new Atom("O"))
        molecule.addAtom(new Atom("O"))


        molecule.addBond(0,1,IBond.Order.SINGLE)
        molecule.addBond(0,2,IBond.Order.SINGLE)
        molecule.addBond(0,3,IBond.Order.DOUBLE)
        molecule.addBond(0,4,IBond.Order.SINGLE)

        return molecule
    }

}
