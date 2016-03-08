package curation.rules.adduct.gcms

import curation.AbstractCurationRule
import curation.CurationObject
import curation.actions.AddTagAction
import curation.actions.RemoveTagAction
import moa.Compound
import moa.Spectrum

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 10/17/14
 * Time: 1:04 PM
 */
class GCMSDerivatizationDoesntMatchCompound extends AbstractCurationRule {
    PredictGCMSCompoundRule predictGCMSCompoundRule

    double accuracyInDalton = 1

    GCMSDerivatizationDoesntMatchCompound() {
        super()

        // TODO: Provide a more useful method of displaying this issue
        // this.successAction = new AddTagAction(SUSPECT_VALUE)
        // this.failureAction = new RemoveTagAction(SUSPECT_VALUE)
    }

    @Override
    boolean executeRule(CurationObject toValidate) {
        if (isGCMSSpectra(toValidate)) {
            Spectrum spectrum = toValidate.getObjectAsSpectra()

            if (spectrum.chemicalCompound == null)
                return true

            Compound bio = spectrum.biologicalCompound
            Compound chem = spectrum.chemicalCompound

            Compound pre = spectrum.predictedCompound

            //no predicted compound, let's generate one
            if (pre == null) {
                logger.info("trying to predict compound, since it wasn't set")
                if (predictGCMSCompoundRule.executeRule(toValidate)) {

                    //reload the spectrum
                    spectrum = Spectrum.get(spectrum.id)

                    pre = spectrum.predictedCompound

                    if (pre == null) {
                        return true
                    }
                } else {
                    return true
                }
            }

            double predictedMass = calculateMolareMass(readMolecule(pre))
            double chemicalMass = calculateMolareMass(readMolecule(chem))

            logger.debug("comparing predicted mass of ${predictedMass} to chemical mass of $chemicalMass")
            def result = (Math.abs(predictedMass - chemicalMass) <= accuracyInDalton)

            logger.debug("compound are matching: ${result}")

            if (!result) {
                new AddTagAction("derivative/adduct doesn't match predicted compound!").doAction(new CurationObject(spectrum.chemicalCompound))
            } else {
                new RemoveTagAction("derivative/adduct doesn't match predicted compound!").doAction(new CurationObject(spectrum.chemicalCompound))

            }
            return true

        }
        return true
    }

    @Override
    boolean ruleAppliesToObject(CurationObject toValidate) {
        return toValidate.isSpectra()
    }

    @Override
    String getDescription() {
        return "determines if the chemical compound is actually possible and not just the same as the biological"
    }
}
