package curation.rules.adduct.gcms
import curation.AbstractCurationRule
import curation.CurationObject
import moa.Spectrum
import moa.server.metadata.MetaDataPersistenceService

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 10/17/14
 * Time: 2:38 PM
 */
class PredictedMMinus15Rule extends AbstractCurationRule {
    PredictGCMSCompoundRule predictGCMSCompoundRule

    MetaDataPersistenceService metaDataPersistenceService

    double accuracyInDalton = 1

    /**
     * CH3 group
     */
    private double amount = 15

    @Override
    boolean executeRule(CurationObject toValidate) {


        Spectrum spectrum = toValidate.getObjectAsSpectra()

        //calculate the predicted compound in case it's missing
        if (spectrum.predictedCompound == null) {
            if (predictGCMSCompoundRule.executeRule(toValidate)) {

                //reload the spectrum
                spectrum = Spectrum.get(spectrum.id)

                if (spectrum.predictedCompound == null) {
                    logger.warn("not able to predict a compound!")
                    return true
                }
            }
        }

        //calculate our predicted mass
        double predictedMass = calculateMolareMass(readMolecule(spectrum.predictedCompound))

        String[] ions = spectrum.spectrum.split(" ")

        //search for the M-15 ion
        for (String ion : ions) {
            Double current = Double.parseDouble(ion.split(":")[0])

            double mMinus15 = predictedMass - amount
            if (Math.abs(current - mMinus15) < accuracyInDalton) {
                logger.debug("found M-15 at ${mMinus15}")

                addTag(toValidate, "has M-15")

                metaDataPersistenceService.generateMetaDataObject(spectrum, [name: "M-15", value: (mMinus15), category: "annotation", computed: true])


                return true
            }
        }

        //always return true
        return true
    }

    @Override
    boolean ruleAppliesToObject(CurationObject toValidate) {
        return isGCMSSpectra(toValidate)
    }

    @Override
    String getDescription() {
        return "checks for the M-15 ion of the given spectra and computes if the compound is acutally possible"
    }
}
