package curation.rules.meta

import static util.MetaDataFieldNames.*

import curation.actions.MetaDataSuspectAction
import curation.rules.AbstractMetaDataCentricRule
import moa.MetaDataValue
import org.apache.log4j.Logger

/**
 * Created by sajjan on 10/7/14.
 */
class IsColumnValid extends AbstractMetaDataCentricRule {
    private Logger logger = Logger.getLogger(getClass())


    def IsColumnValid() {
        this.successAction = (new MetaDataSuspectAction(COLUMN_NAME, false))
        this.failureAction = new MetaDataSuspectAction(COLUMN_NAME, true)
    }

    @Override
    protected boolean acceptMetaDataValue(MetaDataValue val) {
        String value = val.value.toString();

        // Assumes units of mm
        boolean diameterAndLength = value.find(/(\d+\.?\d*(?: [cmu]?m)?)(?:(?:\s?[xX]\s?)|(?: by ))(\d+\.?\d*\s?[cmu]?m)/)

        // Checks alternate form of only length
        boolean onlyLength = value.find(/L=(\d+\.?\d*\s?[cmu]?m)/)

        logger.debug('Diameter and Length: ' + diameterAndLength)
        logger.debug('Length only: ' + onlyLength)

        if (!onlyLength && !diameterAndLength) {
            ((MetaDataSuspectAction) this.getFailureAction()).setReason("length and diameter were not specified!")
        }
        return (diameterAndLength || onlyLength)
    }

    protected boolean isCorrectMetaDataField(MetaDataValue field) {
        return field.name.toLowerCase() == COLUMN_NAME
    }


    @Override
    String getDescription() {
        return "this rule calculates if the $COLUMN_NAME has a valid column value"
    }
}