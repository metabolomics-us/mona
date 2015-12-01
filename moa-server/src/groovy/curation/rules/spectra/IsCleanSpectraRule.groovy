package curation.rules.spectra

import curation.AbstractCurationRule
import curation.CurationObject
import curation.actions.AddTagAction
import curation.actions.RemoveTagAction
import moa.Spectrum
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
     * low abundance noise in percent in relation to the basepeak
     */
    double lowAbundancePercentage = 2

    /**
     * higher abundance peak limit in percent in relation to the basepeak
     */
    double highAbundancePercentage = 10

    /**
     * percentage of peaks are in the low abundance noise range to be considered noisy
     */
    double lowAbundanceNoisyThreshold = 50

    /**
     * percentage of peaks above the high abundance thresholds to be considered noisy
     */
    double highAbundanceNoisyThreshold = 50

    /**
     * minimum number of peaks to check if an LCMS spectrum is direct
     */
    double minLCMSPeaks = 10


    IsCleanSpectraRule() {
        this.successAction = new RemoveTagAction(DIRTY_SPECTRA)
        this.failureAction = new AddTagAction(DIRTY_SPECTRA)
    }

    @Override
    boolean ruleAppliesToObject(CurationObject toValidate) {
        return toValidate.isSpectra()
    }

    protected boolean failByDefault() {
        return false;
    }

    @Override
    boolean executeRule(CurationObject toValidate) {
        Spectrum spectrum = toValidate.getObjectAsSpectra()

        boolean isGCMS = false
        boolean isLCMS = false

        spectrum.tags.each {
            if (it.text == GCMS_SPECTRA) {
                isGCMS = true
            } else if (it.text == LCMS_SPECTRA) {
                isLCMS = true
            }
        }


        int countOfPeaks = 0
        int countOfLowAbundancePeaks = 0
        int countOfHighAbundancePeaks = 0

        spectrum.ions.each {
            logger.info("m/z: ${it.mass}, intensity: ${it.intensity}")
            countOfPeaks++

            if (it.intensity * 100 < lowAbundancePercentage) {
                countOfLowAbundancePeaks++
            } else if (it.intensity * 1000 > highAbundancePercentage) {
                countOfHighAbundancePeaks++
            }
        }

        try {
            double lowAbundanceRatio = 100.0 * countOfLowAbundancePeaks / countOfPeaks
            double highAbundanceRatio = 100.0 * countOfHighAbundancePeaks / countOfPeaks

            boolean isClean = true

            if (isLCMS) {
                if (countOfPeaks >= minLCMSPeaks && lowAbundanceRatio >= lowAbundanceNoisyThreshold)
                    isClean = false
            } else if (isGCMS) {
                if (highAbundanceRatio >= highAbundanceNoisyThreshold)
                    isClean = false
            } else {
                if (lowAbundanceRatio >= lowAbundanceNoisyThreshold || highAbundanceRatio >= highAbundanceNoisyThreshold)
                    isClean = false
            }

            logger.info("low abundance noise ratio: ${lowAbundanceRatio}, low abundance noise ratio: ${highAbundanceRatio}")
            logger.info(isClean ? "\t => clean!" : "\t => dirty!")

            return isClean
        } catch (Exception e){
            logger.error("something wrong with this spectrum: ${spectrum.id}")
            logger.error("spectra: " + spectrum.spectrum)
            logger.error(e.getMessage(),e);

            return true;
        }
    }


    @Override
    String getDescription() {
        return "this rule calculates if the spectra of the spectrum is dirty or not"
    }
}
