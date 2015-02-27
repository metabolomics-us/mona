package curation.rules.adduct.gcms

import curation.AbstractCurationRule
import curation.CurationObject
import curation.actions.AddTagAction
import curation.actions.RemoveTagAction
import moa.Compound
import moa.MetaDataValue
import moa.Spectrum
import moa.server.caluclation.CompoundPropertyService
import org.openscience.cdk.Molecule
import util.chemical.Derivatizer

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 10/16/14
 * Time: 2:00 PM
 */
class PredictGCMSCompoundRule extends AbstractCurationRule {

    CompoundPropertyService compoundPropertyService

    PredictGCMSCompoundRule() {

        this.successAction = new AddTagAction(VIRTUAL_COMPOUND)
        this.failureAction = new RemoveTagAction(VIRTUAL_COMPOUND)
    }

    @Override
    boolean executeRule(CurationObject toValidate) {

        ConfirmGCMSDerivatizationRule confirmGCMSDerivatizationRule = new ConfirmGCMSDerivatizationRule()

        //let's figure our how many tms groups we have
        Spectrum spectrum = toValidate.getObjectAsSpectra()

        for (MetaDataValue val : spectrum.metaData) {

            if (val.getName() == confirmGCMSDerivatizationRule.getField()) {

                //let's ensure this object is valid
                if (confirmGCMSDerivatizationRule.executeRule(toValidate)) {

                    logger.debug("provided derivatization values are correct")
                    int tmsCount = confirmGCMSDerivatizationRule.calculateTMSCount(val)

                    if (tmsCount > 0) {

                        logger.debug("need to derivatize ${tmsCount} groups")

                        Derivatizer derivatizer = new Derivatizer()

                        //derivatize it and ensure that we only get possible derivatized compounds

                        //our newly derivatized compound
                        Molecule derivatizedCompound = derivatizer.generateTMSDerivatizationProduct(readMolecule(spectrum.biologicalCompound), tmsCount)

                        String inchiKey = calculateInChIKey(derivatizedCompound)

                        //find or create a new compound
                        Compound newCompound = Compound.findOrCreateByInchiKey(inchiKey)

                        logger.debug("compound is ${newCompound}")

                        newCompound.molFile = derivatizer.getMOLFile(derivatizedCompound)

                        String inchiCode = calculateInChICode(derivatizedCompound)

                        //assign inchiCode to compound

                        //save our compound
                        newCompound.save(flush: true)

                        //adds the tag
                        new AddTagAction(VIRTUAL_COMPOUND).doAction(new CurationObject(newCompound))

                        compoundPropertyService.calculateMetaData(newCompound)

                        spectrum.predictedCompound = newCompound
                        spectrum.save(flush: true)

                        return true
                    } else {
                        logger.debug("no TMS values specified")
                    }
                }
            }

        }
        return false
    }

    @Override
    boolean ruleAppliesToObject(CurationObject toValidate) {
        return toValidate.isSpectra()
    }

    @Override
    String getDescription() {
        return "tries to predict a chemical compound based on the given rules"
    }
}
