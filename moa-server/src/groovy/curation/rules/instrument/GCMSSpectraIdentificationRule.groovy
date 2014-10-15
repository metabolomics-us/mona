package curation.rules.instrument
import curation.CurationAction
import curation.actions.AddTagAction
import curation.actions.RemoveTagAction
import curation.rules.AbstractMetaDataCentricRule
import moa.MetaDataValue
/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 9/30/14
 * Time: 11:36 AM
 */
class GCMSSpectraIdentificationRule extends AbstractMetaDataCentricRule {

    def GCMSSpectraIdentificationRule() {
        super(new AddTagAction(GCMS_SPECTRA), new RemoveTagAction(GCMS_SPECTRA));
    }

    def GCMSSpectraIdentificationRule(CurationAction successAction, CurationAction failureAction) {
        super(successAction, failureAction)
    }

    @Override
    protected boolean acceptMetaDataValue(MetaDataValue val) {
        String value = val.value.toString().toLowerCase()

        if (value.contains("gcms")) {
            return true
        } else if (value.contains("gc")) {
            return true
        }

        else {
            return false
        }

    }

    /**
     * trying to find out which metadata fields can contain valid values
     * @param field
     * @return
     */
    protected boolean isCorrectMetaDataField(MetaDataValue field) {
        if (field.name.toLowerCase() == "instrument") {
            return true
        } else if (field.name.toLowerCase() == "instrument type") {
            return true
        }
        return false;
    }


    @Override
    String getDescription() {
        return "this rule calculates if the Spectrum comes from a GCMS system"
    }

}
