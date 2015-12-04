package curation.rules.instrument

import moa.MetaDataValue
import org.apache.log4j.Logger
import curation.CurationAction
import curation.actions.AddTagAction
import curation.actions.RemoveTagAction
import curation.rules.AbstractMetaDataCentricRule

import java.util.regex.Pattern
import static util.MetaDataFieldNames.*

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 9/30/14
 * Time: 11:36 AM
 */
class LCMSSpectraIdentificationRule extends AbstractMetaDataCentricRule {
    private Logger logger = Logger.getLogger(getClass())

    static Map<String, List<String>> listOfAcceptedField = new HashMap<>()

    static {
        listOfAcceptedField.put INSTRUMENT, [".*lcms.*", ".*lc/ms.*", ".*ltq.*"]
        listOfAcceptedField.put INSTRUMENT_TYPE, [".*lc.*"]
        listOfAcceptedField.put SOLVENT, [".*"]
        listOfAcceptedField.put "*", ["direct infusion"]
        listOfAcceptedField.put MOBILE_PHASE_A, [","]

    }

    def LCMSSpectraIdentificationRule() {
        super()
        this.successAction = new AddTagAction(LCMS_SPECTRA)
        this.failureAction = new RemoveTagAction(LCMS_SPECTRA)
    }


    @Override
    protected boolean acceptMetaDataValue(MetaDataValue val) {
        String value = val.value.toString().toLowerCase()

        List<String> list = listOfAcceptedField.get(val.name.toLowerCase())

        for (String s : list) {
            logger.info("checking ${s} vs ${val.value} - ${val.unit}")
            if (value.equals(s.toLowerCase())) {
                return true
            } else if (val.unit != null && val.unit.toLowerCase().equals(s.toLowerCase())) {
                return true
            } else if (value.matches(s)) {
                return true
            } else if (value.contains(s.toLowerCase())) {
                return true
            } else {
                Pattern p = Pattern.compile(s, Pattern.CASE_INSENSITIVE);

                if (p.matcher(value)) {
                    return true
                }
            }
        }

        return false
    }

    /**
     * trying to find out which metadata fields can contain valid values
     * @param field
     * @return
     */
    protected boolean isCorrectMetaDataField(MetaDataValue field) {
        for (String s in listOfAcceptedField.keySet()) {
            if (field.name.toLowerCase().equals(s.toLowerCase())) {
                return true
            }
        }

        //any field
        if ("*" in listOfAcceptedField.keySet()) {
            return true
        }

        return false;
    }


    @Override
    String getDescription() {
        return "this rule calculates if the Spectrum comes from a LCMS system, based on the following patterns for fields: ${listOfAcceptedField}"
    }

    protected boolean failOnInvalidValue() {
        return false
    }
}