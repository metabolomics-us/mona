package curation.rules.instrument

import curation.CurationAction
import curation.actions.AddTagAction
import curation.actions.RemoveTagAction
import curation.rules.AbstractMetaDataCentricRule
import moa.MetaDataValue
import static util.MetaDataFieldNames.*

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 9/30/14
 * Time: 11:36 AM
 */
class GCMSSpectraIdentificationRule extends AbstractMetaDataCentricRule {

    static Map<String, String> listOfAcceptedField = new HashMap<>();

    static {
        listOfAcceptedField.put(INSTRUMENT, ".*gcms.*")
        listOfAcceptedField.put(INSTRUMENT_TYPE, ".*gc.*")
        listOfAcceptedField.put(IONIZATION_ENERGY, "ev")
    }

    def GCMSSpectraIdentificationRule() {
        this.successAction = (new AddTagAction(GCMS_SPECTRA))
        this.failureAction = new RemoveTagAction(GCMS_SPECTRA)
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
        } else if (value.contains(s.toLowerCase())) {
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
                logger.info("found field: ${field.name}")
                return true
            }
        }

        return false;
    }


    @Override
    String getDescription() {
        return "this rule calculates if the Spectrum comes from a GCMS system, the utilized fields are: ${listOfAcceptedField}"
    }

    @Override
    protected boolean failOnInvalidValue() {
        return false
    }
}
