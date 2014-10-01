package validation.rules.instrument
import moa.MetaDataValue
import validation.ValidationAction
import validation.actions.AddTagAction
import validation.actions.RemoveTagAction
import validation.rules.AbstractMetaDataCentricRule
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
    def GCMSSpectraIdentificationRule(ValidationAction successAction, ValidationAction failureAction) {
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
