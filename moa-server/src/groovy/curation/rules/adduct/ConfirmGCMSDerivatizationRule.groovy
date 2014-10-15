package curation.rules.adduct

import curation.CurationObject
import curation.actions.AddTagAction
import curation.actions.MetaDataSuspectAction
import curation.rules.AbstractMetaDataCentricRule
import moa.MetaDataValue
import moa.Spectrum
import org.openscience.cdk.Molecule
import util.chemical.Derivatizer
import util.chemical.FunctionalGroupBuilder

/**
 * checks if the provided gcms curation data are actually correct and possible
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 10/15/14
 * Time: 1:15 PM
 */
class ConfirmGCMSDerivatizationRule extends AbstractMetaDataCentricRule {

    String field

    /**
     * do we automatically tag data
     */
    boolean enableTagging = true

    ConfirmGCMSDerivatizationRule() {

        this("derivative type")
    }

    ConfirmGCMSDerivatizationRule(String field) {
        super(new MetaDataSuspectAction(field, false), new MetaDataSuspectAction(field, true))
        this.field = field
    }

    @Override
    protected boolean acceptMetaDataValue(MetaDataValue value) {


        String stringValue = value.getValue().toString()

        def matcher = (stringValue =~ /([0-9]+).*TMS/)
        if (matcher.matches()) {

            int count = Integer.parseInt(matcher[0][1].toString())

            logger.info("found TMS count times: ${count}")


            Spectrum spectrum = value.owner
            Molecule molecule = readMolecule(spectrum.biologicalCompound)
            Derivatizer derivatizer = new Derivatizer()

            logger.info("=> validate hyrdroxyl groups")
            int hydroxylGroups = derivatizer.derivatizeWithTMS(molecule, [FunctionalGroupBuilder.makeHydroxyGroup()]).size()

            logger.info("=> found ${hydroxylGroups} hyrdoxy groups and specified is ${count} TMS")
            //hydroxyl groups always get all derivatized at once!
            if (count < hydroxylGroups) {
                if (enableTagging) {
                    new AddTagAction("invalid derivatization").doAction(new CurationObject(spectrum))
                } else {
                    logger.debug("automatic tagging is disabled")
                }
                logger.debug("hydroxyl group validation failed")
                return false
            } else {
                logger.debug("=>hydroxyl groups are accpeted!")
            }


        } else {
            logger.warn("invalid value specified for field '${field}' and value '${stringValue}'")
        }
        return true
    }

    @Override
    protected boolean isCorrectMetaDataField(MetaDataValue field) {
        return (field.owner instanceof Spectrum &&
                field.category == "focused ion" && field.name == this.field)

    }
    @Override
    String getDescription() {
        return "this rule tries to calculate if the specified ${field} value is actually possible, based on the provided biological compound"
    }
}
