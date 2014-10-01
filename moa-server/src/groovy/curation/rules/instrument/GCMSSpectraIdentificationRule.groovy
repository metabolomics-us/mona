package curation.rules.instrument
import moa.MetaDataValue
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
class GCMSSpectraIdentificationRule extends AbstractMetaDataCentricRule{

    def GCMSSpectraIdentificationRule(){
        super(new AddTagAction("GCMS"),new RemoveTagAction("GCMS"));
    }
    def GCMSSpectraIdentificationRule(CurationAction successAction, CurationAction failureAction) {
        super(successAction, failureAction)
    }

    @Override
    protected boolean acceptMetaDataValue(MetaDataValue val) {
        if (val.name.toLowerCase() == "instrument") {
            String value = val.value.toString().toLowerCase()

            return (value.contains("gcms"))

        }

        return false
    }
}
