package curation.rules.spectra

import curation.AbstractCurationRule
import curation.CurationObject
import curation.actions.MetaDataSuspectAction
import moa.MetaDataValue
import moa.Spectrum
import util.MetaDataFieldNames

import static util.MetaDataFieldNames.COLUMN_NAME
import static util.MetaDataFieldNames.COLUMN_NAME
import static util.MetaDataFieldNames.EXACT_MASS

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 7/31/15
 * Time: 2:20 PM
 */
class ExactMassIsCorrectRule extends AbstractCurationRule {

    /**
     * the accuracy needs to be between 0 and the provided value
     */
    private double requiredPrecessionInPPM = 5;

    public ExactMassIsCorrectRule(){
        this.successAction = (new MetaDataSuspectAction(EXACT_MASS, false))
        this.failureAction = new MetaDataSuspectAction(EXACT_MASS, true)
    }
    @Override
    boolean executeRule(CurationObject toValidate) {

        Spectrum spectrum = toValidate.getObjectAsSpectra();

        for(MetaDataValue value : spectrum.listAvailableValues()){
            if(value.getName().toLowerCase().equals(MetaDataFieldNames.MASS_ACCURACY)){
                double v = Math.abs(Double.parseDouble(value.getValue().toString()));


                if(v > requiredPrecessionInPPM){
                    logger.info("spectra is not precise enough: ${requiredPrecessionInPPM}. It's only ${v}")
                    ((MetaDataSuspectAction) this.getFailureAction()).setReason("this mass, was considered to be not precise enough!")

                    return false
                }
                else{
                    logger.info("spectra is considered precise enough, accuracy was ${v} ppm")
                    return true;
                }
            }
        }

        return true
    }

    @Override
    boolean ruleAppliesToObject(CurationObject toValidate) {
        return toValidate.isSpectra()
    }

    @Override
    String getDescription() {
        return "computes if the provided exact mass is precisse enough. It's accuracy has to be between 0 and ${requiredPrecessionInPPM} ppm"
    }
}
