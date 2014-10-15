package curation.rules.meta

import curation.actions.MetaDataSuspectAction
import curation.rules.AbstractMetaDataCentricRule
import moa.MetaDataValue
import org.apache.log4j.Logger
/**
 * Created by sajjan on 10/7/14.
 */
class IsColumnValid extends AbstractMetaDataCentricRule {
    private Logger logger = Logger.getLogger(getClass())

    private static final String fieldName = "column name"


    def IsColumnValid() {
        super(new MetaDataSuspectAction(fieldName, false), new MetaDataSuspectAction(fieldName, true));
    }

    @Override
    protected boolean acceptMetaDataValue(MetaDataValue val) {
        String value = val.value.toString();

        // Assumes units of mm
        boolean diameterAndLength =  value.find(/(\d+\.?\d*(?: [cmu]?m)?)(?:(?:\s?[xX]\s?)|(?: by ))(\d+\.?\d*\s?[cmu]?m)/)

        // Checks alternate form of only length
        boolean onlyLength = value.find(/L=(\d+\.?\d*\s?[cmu]?m)/)

        logger.debug('Diameter and Length: '+ diameterAndLength)
        logger.debug('Length only: '+ onlyLength)

        return (diameterAndLength || onlyLength)
    }

    protected boolean isCorrectMetaDataField(MetaDataValue field) {
        return field.name.toLowerCase() == fieldName
    }


    @Override
    String getDescription() {
        return "this rule calculates if the $fieldName has a valid column value"
    }
}