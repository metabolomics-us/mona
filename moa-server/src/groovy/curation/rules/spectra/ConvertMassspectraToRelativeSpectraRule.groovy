package curation.rules.spectra
import curation.AbstractCurationRule
import curation.CurrationObject
import curation.actions.AddTagAction
import moa.Spectrum
import moa.Tag
import org.apache.log4j.Logger

import java.text.DecimalFormat
/**
 * converts our massspec to be a relative mass spectra.
 * It also adds a label that this is a relative spectra
 *
 * User: wohlgemuth
 * Date: 10/1/14
 * Time: 1:15 PM
 */
class ConvertMassspectraToRelativeSpectraRule extends AbstractCurationRule {
    private Logger logger = Logger.getLogger(getClass())

    ConvertMassspectraToRelativeSpectraRule() {
        this.successAction = new AddTagAction(RELATIVE_SPECTRA)
    }

    @Override
    boolean ruleAppliesToObject(CurrationObject toValidate) {
        return toValidate.isSpectra()
    }

    @Override
    boolean executeRule(CurrationObject toValidate) {

        Spectrum spectrum = toValidate.getObjectAsSpectra()

        logger.info("checking if the spectra is relative...")
        String massSpec = spectrum.spectrum

        int max = 0

        massSpec.split(" ").each { s ->
            double d = Double.parseDouble(s.split(":")[1])

            if (d >= max) {
                max = d

            }

        }

        //this is an absolute spectra
        if (!spectrum.getTags().contains(Tag.findByText(RELATIVE_SPECTRA))) {
            logger.info("=> spectra needs converting")
            StringBuilder result = new StringBuilder()

            massSpec.split(" ").each { s ->
                def i = s.split(":")

                if (i.size() > 1) {
                    double ion = Double.parseDouble(i[0])
                    double intensity = Double.parseDouble(i[1])

                    result.append(new BigDecimal(ion).stripTrailingZeros().doubleValue())
                    result.append(":")
                    result.append(new DecimalFormat("#.###").format(new BigDecimal(intensity / max * 100).stripTrailingZeros().doubleValue()))

                    result.append(" ")

                }
            }

            String resultString = result.toString().trim()

            spectrum.spectrum = resultString
            spectrum.save(flush: true)
            logger.info("\t=> done was converted to relative")
        } else {
            logger.info("\t=> done was relative")
        }

        return true
    }
}
