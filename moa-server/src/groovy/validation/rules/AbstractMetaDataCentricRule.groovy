package validation.rules

import moa.MetaDataValue
import moa.Spectrum
import org.apache.log4j.Logger
import validation.AbstractValidationRule
import validation.ValidationAction

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 9/30/14
 * Time: 1:20 PM
 */
abstract class AbstractMetaDataCentricRule extends AbstractValidationRule {

    private Logger logger = Logger.getLogger(getClass())

    public AbstractMetaDataCentricRule(ValidationAction successAction, ValidationAction failureAction) {
        super(successAction, failureAction)
    }

    /**
     * does the actual check
     * @param value
     * @return
     */
    protected abstract boolean acceptMetaDataValue(MetaDataValue value);

    @Override
    final boolean executeRule(Spectrum spectrum) {

        for (MetaDataValue metaDataValue : spectrum.getMetaData()) {
            if (isCorrectMetaDataField(metaDataValue)) {
                return (acceptMetaDataValue(metaDataValue))
            }
        }
        return !failByDefault()
    }

    /**
     * do we accepts the given field? Usful to limit execution to certain fields only
     * @param field
     * @return
     */
    protected boolean isCorrectMetaDataField(MetaDataValue field) {
        return true
    }

}
