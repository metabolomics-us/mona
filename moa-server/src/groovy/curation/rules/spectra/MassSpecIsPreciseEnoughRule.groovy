package curation.rules.spectra

import curation.CurationObject
import moa.Spectrum
import org.apache.log4j.Logger
import curation.AbstractCurationRule

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 9/30/14
 * Time: 1:47 PM
 */
class MassSpecIsPreciseEnoughRule extends AbstractCurationRule {

    Logger logger = Logger.getLogger(getClass())
    double minPrecision = 3

    @Override
    boolean ruleAppliesToObject(CurationObject toValidate) {
        return toValidate.isSpectra()
    }

    @Override
    boolean executeRule(CurationObject toValidate) {

        Spectrum spectrum = toValidate.getObjectAsSpectra()

        logger.debug("checking precision of mass spec...")

        String spectra = spectrum.spectrum

        boolean result = false

        spectra.split(" ").each { String ionPair ->

            String ion = ionPair.split(":")[0]
            if (ion.indexOf(".") > 0) {


                String ionDigits = (ion.substring(ion.indexOf('.') + 1, ion.length() - 1))

                if (ionDigits.length() > minPrecision) {
                    if (Integer.parseInt(ionDigits) > 0) {
                        result = true
                    }
                }
            }
        }

        logger.debug("considert to be precise enough: ${result}")
        return result
    }


    @Override
    String getDescription() {
        return "this rule calculates if the spectra of a spectrum is precise enough"
    }
}
