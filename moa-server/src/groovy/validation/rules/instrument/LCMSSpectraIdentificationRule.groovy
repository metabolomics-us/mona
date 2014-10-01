package validation.rules.instrument
import moa.MetaDataValue
import org.apache.log4j.Logger
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
class LCMSSpectraIdentificationRule extends AbstractMetaDataCentricRule {
    private Logger logger = Logger.getLogger(getClass())


    def LCMSSpectraIdentificationRule() {
        super(new AddTagAction("LCMS"), new RemoveTagAction("LCMS"));
    }

    def LCMSSpectraIdentificationRule(ValidationAction successAction, ValidationAction failureAction) {
        super(successAction, failureAction)
    }

    @Override
    protected boolean acceptMetaDataValue(MetaDataValue val) {
        if (val.name.toLowerCase() == "instrument") {
            String value = val.value.toString().toLowerCase()

            return (value.contains("lcms"))

        }

        return false
    }
}