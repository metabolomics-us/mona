package curation

import curation.actions.IgnoreOnFailureAction
import moa.Compound
import moa.Tag
import moa.server.tag.TagService
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
import org.springframework.beans.factory.annotation.Autowired
import util.chemical.Derivatizer
import util.chemical.MolHelper

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

    TagService tagService

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

        return new MolHelper().readMolecule(compound)
    }

    /**
     * calculate the inchi code
     * @param molecule
     * @return
     */
    String calculateInChICode(Molecule molecule) {

        return MolHelper.newInstance().convertToInChICode(molecule)
   }

    /**
     * calculates the inchi key
     * @param molecule
     * @return
     */
    String calculateInChIKey(Molecule molecule) {
        return MolHelper.newInstance().convertToInChIKey(molecule)
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
        if (object.isSupportsMetaDataObject()) {
            tags.each { String tag ->

                tagService.addTagTo(tag, object.getObjectAsSupportsMetaData())
            }
        }
    }

    protected void removeTag(CurationObject object, String... tags) {
        if (object) {
            tags.each {
                tagService.removeTagFrom(it, object.getObjectAsSupportsMetaData())
            }
        }
    }

}
