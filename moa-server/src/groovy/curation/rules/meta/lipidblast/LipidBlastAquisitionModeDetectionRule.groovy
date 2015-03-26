package curation.rules.meta.lipidblast

import curation.actions.MetaDataSuspectAction
import curation.rules.AbstractMetaDataCentricRule
import moa.MetaDataValue
import moa.server.metadata.MetaDataPersistenceService
import org.apache.log4j.Logger

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 3/24/15
 * Time: 1:07 PM
 */
class LipidBlastAquisitionModeDetectionRule  extends AbstractMetaDataCentricRule {

    private Logger logger = Logger.getLogger(getClass())

    private static final String FIELD = "precursortype"

    private static final String ION_MODE = "ion mode"

    MetaDataPersistenceService metaDataPersistenceService

    LipidBlastAquisitionModeDetectionRule() {
        super(new MetaDataSuspectAction(FIELD, false), new MetaDataSuspectAction(FIELD, true))

    }

    @Override
    protected boolean acceptMetaDataValue(MetaDataValue value) {

        boolean hasIonModeSpecified = false
        value.getOwner().metaData.each {MetaDataValue v ->

            if(v.getName() == "ion mode"){
                hasIonModeSpecified = true
            }
        }

        if(!hasIonModeSpecified) {
            //positive mode
            if (value.getValue().toString().trim().endsWith("+")) {
                metaDataPersistenceService.generateMetaDataObject(value.owner,[name:ION_MODE,value:"positive",computed:true])
            }
            //negative mode
            else if (value.getValue().toString().trim().endsWith("-")) {
                metaDataPersistenceService.generateMetaDataObject(value.owner,[name:ION_MODE,value:"negative",computed:true])
            }
        }
        else{
            logger.info("ion mode was already specified")
        }
        return true
    }

    /**
     * checks if we can accept this field
     * @param value
     * @return
     */
    protected boolean isCorrectMetaDataField(MetaDataValue value) {
        logger.debug("checking ${value.name} against defined field ${FIELD}")
        if (value.name.toLowerCase().equals(FIELD.toLowerCase())) {
            return true
        }
        return false
    }

    //if the field doesn't exist, no reason to fail the rule
    protected boolean failByDefault() {
        return false;
    }


    @Override
    String getDescription() {
        return "this rule utilizes the precursor type to estimate if the data where acquired in positive or negative mode"
    }

}
