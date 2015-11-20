package curation.rules.spectra

import curation.CurationObject
import moa.Ion
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
    int minPrecision = 3

    @Override
    boolean ruleAppliesToObject(CurationObject toValidate) {
        return toValidate.isSpectra()
    }

    @Override
    boolean executeRule(CurationObject toValidate) {
        Spectrum spectrum = toValidate.getObjectAsSpectra()

        logger.debug("checking precision of mass spec...")

        boolean result = false

        def pattern = /[0-9]+.[0-9]{${minPrecision}}/
        spectrum.ions.each { Ion i ->

            String ion = i.mass.toString()

            def matcher = (ion =~ pattern)
            if(matcher.matches()){
                result = true
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
