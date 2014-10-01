package curation.rules.spectra
import curation.AbstractCurationRule
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
class IsCleanSpectraRule extends AbstractCurationRule{

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
        super(new RemoveTagAction("dirty spectra"),new AddTagAction("dirty spectra"))
    }

    @Override
    boolean executeRule(Spectrum spectrum) {

        if(spectrum.getTags().contains(Tag.findByText(RELATIVE_SPECTRA))) {

            int countOfPeaks = 0
            int countOfNoisyPeaks = 0

            spectrum.spectrum.split(" ").each {
                countOfPeaks++

                if (Double.parseDouble(it.split(":")[1]) < noisePercentage) {
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
        else{
            throw new RuntimeException("not a valid spectra, it needs to be relative. Please configure your workflow right!")
        }
    }

}
