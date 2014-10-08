package curation.rules
import curation.AbstractCurationRule
import curation.CurationAction
import curation.CurrationObject
import moa.MetaDataValue
import moa.Spectrum
import org.apache.log4j.Logger
/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 9/30/14
 * Time: 1:20 PM
 */
abstract class AbstractMetaDataCentricRule extends AbstractCurationRule {

    private Logger logger = Logger.getLogger(getClass())

    public AbstractMetaDataCentricRule(CurationAction successAction, CurationAction failureAction) {
        super(successAction, failureAction)
    }

    /**
     * does the actual check
     * @param value
     * @return
     */
    protected abstract boolean acceptMetaDataValue(MetaDataValue value);

    @Override
    boolean ruleAppliesToObject( CurrationObject object) {
        return object.isSpectra()
    }

    @Override
    final boolean executeRule(CurrationObject toValidate) {

        Spectrum spectrum = toValidate.getObjectAsSpectra()

        for (MetaDataValue metaDataValue : spectrum.getMetaData()) {

            logger.debug("checking for correct meta data value field: ${metaDataValue.name}")
            if (isCorrectMetaDataField(metaDataValue)) {
                logger.debug("\t=> accepted, checking actual value")
                if (acceptMetaDataValue(metaDataValue)) {
                    logger.debug("\t\t=> value was ok")
                    return true
                }
                else{
                    logger.debug("\t\t=> value was not accepted, moving on")
                }
            }
            else{
                logger.debug("\t=> wrong field, moving on")
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
