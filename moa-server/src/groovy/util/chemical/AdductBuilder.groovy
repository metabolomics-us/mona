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
     * Definitions of positive mode lcms adducts
     * @link http://fiehnlab.ucdavis.edu/staff/kind/Metabolomics/MS-Adduct-Calculator/
     */
    public static final LCMS_POSITIVE_ADDUCTS = [
            "[M+3H]+": {double M -> M / 3.0 + 1.007276},
            "[M+2H+Na]+": {double M -> M / 3.0 + 8.334590},
            "[M+H+2Na]+": {double M -> M / 3 + 15.7661904},
            "[M+3Na]+": {double M -> M / 3.0 + 22.989218},
            "[M+2H]+": {double M -> M / 2.0 + 1.007276},
            "[M+H+NH4]+": {double M -> M / 2.0 + 9.520550},
            "[M+H+Na]+": {double M -> M / 2.0 + 11.998247},
            "[M+H+K]+": {double M -> M / 2.0 + 19.985217},
            "[M+ACN+2H]+": {double M -> M / 2.0 + 21.520550},
            "[M+2Na]+": {double M -> M / 2.0 + 22.989218},
            "[M+2ACN+2H]+": {double M -> M / 2.0 + 42.033823},
            "[M+3ACN+2H]+": {double M -> M / 2.0 + 62.547097},
            "[M+H]+": {double M -> M + 1.007276},
            "[M+NH4]+": {double M -> M + 18.033823},
            "[M+Na]+": {double M -> M + 22.989218},
            "[M+CH3OH+H]+": {double M -> M + 33.033489},
            "[M+K]+": {double M -> M + 38.963158},
            "[M+ACN+H]+": {double M -> M + 42.033823},
            "[M+2Na-H]+": {double M -> M + 44.971160},
            "[M+IsoProp+H]+": {double M -> M + 61.06534},
            "[M+ACN+Na]+": {double M -> M + 64.015765},
            "[M+2K-H]+": {double M -> M + 76.919040},
            "[M+DMSO+H]+": {double M -> M + 79.02122},
            "[M+2ACN+H]+": {double M -> M + 83.060370},
            "[M+IsoProp+Na+H]+": {double M -> M + 84.05511},
            "[2M+H]+": {double M -> 2 * M + 1.007276},
            "[2M+NH4]+": {double M -> 2 * M + 18.033823},
            "[2M+Na]+": {double M -> 2 * M + 22.989218},
            "[2M+3H2O+2H]+": {double M -> 2 * M + 28.02312},
            "[2M+K]+": {double M -> 2 * M + 38.963158},
            "[2M+ACN+H]+": {double M -> 2 * M + 42.033823},
            "[2M+ACN+Na]+": {double M -> 2 * M + 64.015765}
    ]

    /**
     * Definitions of negative mode lcms adducts
     * @link http://fiehnlab.ucdavis.edu/staff/kind/Metabolomics/MS-Adduct-Calculator/
     */
    public static final LCMS_NEGATIVE_ADDUCTS = [
            "[M-3H]-": {double M -> M / 3.0 - 1.007276},
            "[M-2H]-": {double M -> M / 2.0 - 1.007276},
            "[M-H2O-H]-": {double M -> M - 19.01839},
            "[M-H]-": {double M -> M - 1.007276},
            "[M+Na-2H]-": {double M -> M + 20.974666},
            "[M+Cl]-": {double M -> M + 34.969402},
            "[M+K-2H]-": {double M -> M + 36.948606},
            "[M+FA-H]-": {double M -> M + 44.998201},
            "[M+Hac-H]-": {double M -> M + 59.013851},
            "[M+Br]-": {double M -> M + 78.918885},
            "[M+TFA-H]-": {double M -> M + 112.985586},
            "[2M-H]-": {double M -> 2 * M - 1.007276},
            "[2M+FA-H]-": {double M -> 2 * M + 44.998201},
            "[2M+Hac-H]-": {double M -> 2 * M + 59.013851},
            "[3M-H]-": {double M -> 3 * M - 1.007276},
            "[M+CH3OH+H]-": {double M -> M + 33.033489},
            "[M+K]-": {double M -> M + 38.963158},
            "[M+ACN+H]-": {double M -> M + 42.033823},
            "[M+2Na-H]-": {double M -> M + 44.971160},
            "[M+IsoProp+H]-": {double M -> M + 61.06534},
            "[M+ACN+Na]-": {double M -> M + 64.015765},
            "[M+2K-H]-": {double M -> M + 76.919040},
            "[M+DMSO+H]-": {double M -> M + 79.02122},
            "[M+2ACN+H]-": {double M -> M + 83.060370},
            "[M+IsoProp+Na+H]-": {double M -> M + 84.05511},
            "[2M+H]-": {double M -> 2 * M + 1.007276},
            "[2M+NH4]-": {double M -> 2 * M + 18.033823},
            "[2M+Na]-": {double M -> 2 * M + 22.989218},
            "[2M+3H2O+2H]-": {double M -> 2 * M + 28.02312},
            "[2M+K]-": {double M -> 2 * M + 38.963158},
            "[2M+ACN+H]-": {double M -> 2 * M + 42.033823},
            "[2M+ACN+Na]-": {double M -> 2 * M + 64.015765}
    ]


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
