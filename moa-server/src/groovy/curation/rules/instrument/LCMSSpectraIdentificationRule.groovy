package curation.rules.instrument

import moa.MetaDataValue
import org.apache.log4j.Logger
import curation.CurationAction
import curation.actions.AddTagAction
import curation.actions.RemoveTagAction
import curation.rules.AbstractMetaDataCentricRule

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 9/30/14
 * Time: 11:36 AM
 */
class LCMSSpectraIdentificationRule extends AbstractMetaDataCentricRule {
    private Logger logger = Logger.getLogger(getClass())

    Map<String, String> listOfAcceptedField = ["instrument": ".*lcms.*", "instrument type": ".*lc.*", "solvent": ".*"]


    def LCMSSpectraIdentificationRule() {
        super(new AddTagAction(LCMS_SPECTRA), new RemoveTagAction(LCMS_SPECTRA));
    }

    def LCMSSpectraIdentificationRule(CurationAction successAction, CurationAction failureAction) {
        super(successAction, failureAction)
    }

    @Override
    protected boolean acceptMetaDataValue(MetaDataValue val) {
        String value = val.value.toString().toLowerCase()


        String s = listOfAcceptedField.get(val.name.toLowerCase())

        logger.info("checking ${s} vs ${val.value} - ${val.unit}")

        if (value.equals(s.toLowerCase())) {

            return true
        } else if (val.unit != null && val.unit.toLowerCase().equals(s.toLowerCase())) {
            return true
        } else if (value.matches(s)) {
            return true
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

        return false;
    }


    @Override
    String getDescription() {
        return "this rule calculates if the Spectrum comes from a LCMS system"
    }
}