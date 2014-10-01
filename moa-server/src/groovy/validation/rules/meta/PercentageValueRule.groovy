package validation.rules.meta

import moa.MetaDataValue
import org.apache.log4j.Logger
import validation.actions.MetaDataSuspectAction
import validation.rules.AbstractMetaDataCentricRule

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 9/30/14
 * Time: 2:08 PM
 */
class PercentageValueRule extends AbstractMetaDataCentricRule {

    private Logger logger = Logger.getLogger(getClass())
    private String field

    double maxPercentage = 100
    double minPercentage = 0

    PercentageValueRule(String field) {
        super(new MetaDataSuspectAction(field, false), new MetaDataSuspectAction(field, true))
        this.field = field
    }

    @Override
    protected boolean acceptMetaDataValue(MetaDataValue value) {
        Double val = Double.parseDouble(value.getValue().toString())

        logger.info("\t\t=>checking ${val} to be >= ${minPercentage} and <= ${maxPercentage}")
        return (val >= minPercentage && val <= maxPercentage)
    }

    /**
     * checks if we can accept this field
     * @param value
     * @return
     */
    protected boolean isCorrectMetaDataField(MetaDataValue value) {
        logger.info("checking ${value.name} against defined field ${field}")
        if (value.name.toLowerCase().equals(field.toLowerCase())) {

            logger.info("\t=>checking if it's the correct unit: ${value.unit}")
            if (value.unit != null && value.unit.toLowerCase().trim().equals("%")) {
                return true;
            } else {
                logger.info("\t\t => wrong unit, rule is ignored")
                return false
            }
        } else {
            logger.info("\t => wrong field, rule is ignored")
        }
        return false
    }

    //if the field doesn't exist, no reason to fail the rule
    protected boolean failByDefault() {
        return false;
    }
}
