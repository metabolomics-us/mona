package curation

import curation.actions.AddTagAction
import curation.actions.IgnoreOnFailureAction
import curation.actions.RemoveTagAction
import moa.Compound
import moa.Tag
import org.apache.log4j.Logger
import org.openscience.cdk.DefaultChemObjectBuilder
import org.openscience.cdk.Molecule
import org.openscience.cdk.inchi.InChIGenerator
import org.openscience.cdk.inchi.InChIGeneratorFactory
import org.openscience.cdk.interfaces.IMolecularFormula
import org.openscience.cdk.io.MDLV2000Reader
import org.openscience.cdk.tools.CDKHydrogenAdder
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator
import util.chemical.Derivatizer

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 9/30/14
 * Time: 11:37 AM
 */
abstract class AbstractCurationRule implements CurationRule {

    Logger logger = Logger.getLogger(getClass())

    CurationAction successAction

    CurationAction failureAction

    /**
     * default constructor
     * @param successAction
     * @param failureAction
     */
    public AbstractCurationRule(CurationAction successAction, CurationAction failureAction) {
        this.successAction = successAction
        this.failureAction = failureAction
    }

    public AbstractCurationRule() {
        this.successAction = new IgnoreOnFailureAction()
        this.failureAction = new IgnoreOnFailureAction()
    }

    @Override
    final CurationAction getSuccessAction() {
        return successAction
    }

    @Override
    final CurationAction getFailureAction() {
        return failureAction
    }

    /**
     * should we fail by default
     * @return
     */
    protected boolean failByDefault() {
        return true;
    }

    /**
     * reads a molecule
     * @param compound
     * @return
     */
    Molecule readMolecule(Compound compound) {

        String molFile = compound.molFile

        if (molFile.startsWith("\n") == false) {
            molFile = "\n" + molFile
        }

        def reader = new MDLV2000Reader(new StringReader(molFile))

        Molecule mol = reader.read(new Molecule())


        AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mol);
        CDKHydrogenAdder.getInstance(DefaultChemObjectBuilder.newInstance()).addImplicitHydrogens(mol)
        AtomContainerManipulator.convertImplicitToExplicitHydrogens(mol);


        return mol
    }

    /**
     * calculate the inchi code
     * @param molecule
     * @return
     */
    String calculateInChICode(Molecule molecule) {

        InChIGeneratorFactory factory = InChIGeneratorFactory.getInstance()
        InChIGenerator gen = factory.getInChIGenerator(molecule);

        return gen.inchi
    }

    /**
     * calculates the inchi key
     * @param molecule
     * @return
     */
    String calculateInChIKey(Molecule molecule) {

        InChIGeneratorFactory factory = InChIGeneratorFactory.getInstance()
        InChIGenerator gen = factory.getInChIGenerator(molecule);

        return gen.inchiKey
    }

    IMolecularFormula calculateFormula(Molecule molecule) {
        IMolecularFormula moleculeFormula = MolecularFormulaManipulator
                .getMolecularFormula(molecule);

        return moleculeFormula;
    }

    /**
     * calculates the sum formula
     * @param molecule
     * @return
     */
    String calculateSumFormulaString(Molecule molecule) {
        return MolecularFormulaManipulator.getString(calculateFormula(molecule))

    }

    /**
     * calculates the molare mass
     * @param molecule
     * @return
     */
    double calculateMolareMass(Molecule molecule) {
        return MolecularFormulaManipulator.getTotalExactMass(calculateFormula(molecule))
    }

    /**
     * is this a gcms spectra
     * @param object
     * @return
     */
    boolean isGCMSSpectra(CurationObject object) {

        if (object.isSpectra()) {

            for (Tag s : object.getObjectAsSpectra().tags) {
                if (s.text == GCMS_SPECTRA) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * is this a lcms spectra
     * @param object
     * @return
     */
    boolean isLCMSSpectra(CurationObject object) {

        if (object.isSpectra()) {

            for (Tag s : object.getObjectAsSpectra().tags) {
                if (s.text == LCMS_SPECTRA) {
                    return true
                }
            }

        }
        return false
    }

    /**
     * calculates the count of functional groups in this molecule
     * @param structure
     * @param groups
     * @return
     */
    int calculateFunctionalGroupCount(Molecule structure, Collection<Molecule> groups) {

        def result = new Derivatizer().derivatizeWithTMS(structure, groups)


        logger.info("received: ${calculateSumFormulaString(structure)}")
        for (Molecule mol : result) {
            logger.info("generated: ${calculateSumFormulaString(mol)}")
        }
        return result.size()
    }

    protected void addTag(CurationObject object, String... tags) {
        new AddTagAction(tags).doAction(object)
    }

    protected void removeTag(CurationObject object, String... tags) {
        new RemoveTagAction(tags).doAction(object)
    }


}
