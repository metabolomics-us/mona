package curation.rules.spectra

import curation.AbstractCurationRule
import curation.CurationObject
import curation.actions.AddTagAction
import curation.actions.RemoveTagAction
import moa.Spectrum
import moa.Tag
import org.apache.log4j.Logger

/**
 * a simple rule to determine if a spectra is dirty or not
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 10/1/14
 * Time: 2:00 PM
 */
class IsCleanSpectraRule extends AbstractCurationRule {

    private Logger logger = Logger.getLogger(getClass())

    /**
     * noise in percent in relation to the basepeak
     */
    double noisePercentage = 2

    /**
     * how many percent of peaks are in the noise range
     */
    double percentOfSpectraIsNoise = 50


    IsCleanSpectraRule() {
        this.successAction = new RemoveTagAction(DIRTY_SPECTRA)
        this.failureAction = new AddTagAction(DIRTY_SPECTRA)
    }

    @Override
    boolean ruleAppliesToObject(CurationObject toValidate) {
        return toValidate.isSpectra()
    }

    @Override
    boolean executeRule(CurationObject toValidate) {
        Spectrum spectrum = toValidate.getObjectAsSpectra()


        int countOfPeaks = 0
        int countOfNoisyPeaks = 0

        spectrum.ions.each {
            countOfPeaks++

            if (Double.parseDouble(it.intensity) < noisePercentage) {
                countOfNoisyPeaks++
            }
        }

        double ratio = countOfNoisyPeaks / countOfPeaks * 100

        logger.info("noise ratio: ${ratio}, spectra is")

        if (ratio > percentOfSpectraIsNoise) {
            logger.info("\t => dirty!")
            return false
        } else {
            logger.info("\t => clean!")
            return true
        }

    }


    @Override
    String getDescription() {
        return "this rule calculates if the spectra of the spectrum is dirty or not"
    }
}
