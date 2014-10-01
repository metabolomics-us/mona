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


    def LCMSSpectraIdentificationRule() {
        super(new AddTagAction("LCMS"), new RemoveTagAction("LCMS"));
    }

    def LCMSSpectraIdentificationRule(CurationAction successAction, CurationAction failureAction) {
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