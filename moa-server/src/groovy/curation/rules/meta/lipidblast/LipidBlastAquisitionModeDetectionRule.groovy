package curation.rules.meta.lipidblast
import static util.MetaDataFieldNames.*

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



    MetaDataPersistenceService metaDataPersistenceService

    LipidBlastAquisitionModeDetectionRule() {
        this.successAction = (new MetaDataSuspectAction(PRECURSOR_TYPE, false))
        this.failureAction = new MetaDataSuspectAction(PRECURSOR_TYPE, true)

    }

    @Override
    protected boolean acceptMetaDataValue(MetaDataValue value) {

        boolean hasIonModeSpecified = false
        value.getOwner().metaData.each {MetaDataValue v ->

            if(v.getName() == ION_MODE){
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
        logger.debug("checking ${value.name} against defined field ${PRECURSOR_TYPE}")
        if (value.name.toLowerCase().equals(PRECURSOR_TYPE.toLowerCase())) {
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
