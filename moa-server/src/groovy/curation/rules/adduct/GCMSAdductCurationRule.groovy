package curation.rules.adduct
import curation.CommonTags
import moa.Compound
import moa.Spectrum
import moa.Tag
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator
import util.chemical.AdductBuilder
import util.chemical.TMSFavoredFunctionalGroups
/**
 *
 * calculates all possible TMS adduct's aswell as ensuring that the right flags were set
 */
class GCMSAdductCurationRule extends AbstractAdductCurationRule {

    @Override
    Map<String, Closure> getAdductTable(String ionMode, Spectrum spectrum) {

        Compound bioLogicalCompount = spectrum.biologicalCompound

        //calculate mass of TMS
        final double tmsMass = MolecularFormulaManipulator.getTotalExactMass(MolecularFormulaManipulator.getMolecularFormula(AdductBuilder.makeTMS()))

        logger.info("mass of tms is: ${tmsMass}")

        Map map = [:]

        //define GCMS groups
        def groups = TMSFavoredFunctionalGroups.buildFavoredGroupsInOrder()

        int countOfPossibleDerivatizations = calculateFunctionalGroupCount(readMolecule(bioLogicalCompount), groups)

        logger.debug("discovered $countOfPossibleDerivatizations possible derivatizations")

        //we like todo things the most intuitive way
        for (int i = 1; i <= countOfPossibleDerivatizations; i++) {
            map.put("M+${i}TMS", { double index, mass, m -> m + (index * mass) }.curry(i, tmsMass))
        }

        return map
    }

    @Override
    boolean requiresIonMode() {
        return false
    }

    @Override
    boolean isValidSpectraForRule(Spectrum spectrum) {
        for (Tag s : spectrum.getTags()) {
            if (s.text == CommonTags.GCMS_SPECTRA) {
                return true
            }
        }

        logger.info("no gcms tag found, so wrong object!")

        return false
    }

    @Override
    String getDescription() {
        return "this rule tries to annotate all GCMS Adducts, found in the spectra"
    }
}
