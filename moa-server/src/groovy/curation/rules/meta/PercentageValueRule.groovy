package curation.rules.meta

import moa.MetaDataValue
import org.apache.log4j.Logger
import curation.actions.MetaDataSuspectAction
import curation.rules.AbstractMetaDataCentricRule

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

        this.getFailureAction().setReason(("value needs to be >= ${minPercentage} and <= ${maxPercentage}"))

        return (val >= minPercentage && val <= maxPercentage)
    }

    /**
     * checks if we can accept this field
     * @param value
     * @return
     */
    protected boolean isCorrectMetaDataField(MetaDataValue value) {
        logger.debug("checking ${value.name} against defined field ${field}")
        if (value.name.toLowerCase().equals(field.toLowerCase())) {

            logger.debug("\t=>checking if it's the correct unit: ${value.unit}")
            if (value.unit != null && value.unit.toLowerCase().trim().equals("%")) {
                return true;
            } else {
                logger.debug("\t\t => wrong unit, rule is ignored")
                return false
            }
        } else {
            logger.debug("\t => wrong field, rule is ignored")
        }
        return false
    }

    //if the field doesn't exist, no reason to fail the rule
    protected boolean failByDefault() {
        return false;
    }


    @Override
    String getDescription() {
        return "this rule calculates if the percentage of the field $field is between $minPercentage and $maxPercentage"
    }
}
