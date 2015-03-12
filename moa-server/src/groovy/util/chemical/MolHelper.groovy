package util.chemical

import moa.Compound
import org.openscience.cdk.DefaultChemObjectBuilder
import org.openscience.cdk.Molecule
import org.openscience.cdk.inchi.InChIGenerator
import org.openscience.cdk.inchi.InChIGeneratorFactory
import org.openscience.cdk.inchi.InChIToStructure
import org.openscience.cdk.io.MDLV2000Reader
import org.openscience.cdk.tools.CDKHydrogenAdder
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 3/11/15
 * Time: 4:35 PM
 */
class MolHelper {

    /**
     * reads a mol file for us from the given compound
     * @param compound
     * @return
     */
    Molecule readMolecule(Compound compound) {

        String molFile = compound.molFile

        if (molFile != null) {
            if (molFile.startsWith("\n") == false) {
                molFile = "\n" + molFile
            }

            def reader = new MDLV2000Reader(new StringReader(molFile))

            Molecule mol = reader.read(new Molecule())


            AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mol);
            CDKHydrogenAdder.getInstance(DefaultChemObjectBuilder.newInstance()).addImplicitHydrogens(mol)
            AtomContainerManipulator.convertImplicitToExplicitHydrogens(mol);

            return mol
        } else {
            throw new NullPointerException("there was no molfile provided for compound: ${compound}")
        }
    }

    /**
     * convert to a molecule
     * @param molecule
     * @return
     */
    String convertToInChICode(Molecule molecule){
        InChIGeneratorFactory factory = InChIGeneratorFactory.getInstance()
        InChIGenerator gen = factory.getInChIGenerator(molecule);

        return gen.inchi

    }

    /**
     * convert to an inchi key
     * @param molecule
     * @return
     */
    String convertToInChIKey(Molecule molecule){
        InChIGeneratorFactory factory = InChIGeneratorFactory.getInstance()
        InChIGenerator gen = factory.getInChIGenerator(molecule);

        return gen.inchiKey

    }

    /**
     * convert to an inchi key
     * @param inchi
     * @return
     */
    String convertToInChIKey(String inchi){
        InChIGeneratorFactory fact = InChIGeneratorFactory.getInstance()
        InChIToStructure structure = fact.getInChIToStructure(inchi,DefaultChemObjectBuilder.newInstance())

        return fact.getInChIGenerator(structure.getAtomContainer()).getInchiKey()

    }



    static MolHelper newInstance(){
        return  new MolHelper()
    }
}
