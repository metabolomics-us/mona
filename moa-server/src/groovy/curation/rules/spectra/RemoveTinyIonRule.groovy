package curation.rules.spectra

import curation.AbstractCurationRule
import curation.CurationObject
import moa.Ion
import moa.Spectrum
import org.apache.log4j.Logger

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 6/18/15
 * Time: 1:13 PM
 */
class RemoveTinyIonRule  extends AbstractCurationRule {

    Logger logger = Logger.getLogger(getClass())
    double maxIntensitity = 0.0001;

    @Override
    boolean ruleAppliesToObject(CurationObject toValidate) {
        return toValidate.isSpectra()
    }

    @Override
    boolean executeRule(CurationObject toValidate) {

        Spectrum spectrum = toValidate.getObjectAsSpectra()

        def toDelete = []

        spectrum.ions.each {Ion ion ->
           if(ion.intensity <= maxIntensitity){
               toDelete.add(ion)
           }
        }

        toDelete.each {
            spectrum.ions.remove(it)
            it.delete()
        }

        return true
    }


    @Override
    String getDescription() {
        return "this rule removes ion's below or equal to the intensity of ${maxIntensitity}"
    }
}
