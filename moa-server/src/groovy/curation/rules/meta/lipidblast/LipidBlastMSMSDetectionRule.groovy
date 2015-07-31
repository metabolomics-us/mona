package curation.rules.meta.lipidblast

import curation.actions.MetaDataSuspectAction
import curation.rules.AbstractMetaDataCentricRule
import moa.MetaDataValue
import moa.server.metadata.MetaDataPersistenceService
import org.apache.log4j.Logger
import static util.MetaDataFieldNames.*

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 3/25/15
 * Time: 10:57 AM
 */
class LipidBlastMSMSDetectionRule extends AbstractMetaDataCentricRule {

    private Logger logger = Logger.getLogger(getClass())

    MetaDataPersistenceService metaDataPersistenceService

    LipidBlastMSMSDetectionRule() {
        this.successAction = new MetaDataSuspectAction(PRECURSORTYPE, false)
        this.failureAction = new MetaDataSuspectAction(PRECURSORTYPE, true)

    }

    @Override
    protected boolean acceptMetaDataValue(MetaDataValue value) {

        boolean hasFieldSpecified = false
        value.getOwner().metaData.each { MetaDataValue v ->

            if (v.getName() == MS_LEVEL) {
                hasFieldSpecified = true
            }
        }

        if (!hasFieldSpecified) {
            //positive mode
            metaDataPersistenceService.generateMetaDataObject(value.owner, [name: MS_LEVEL, value: "MS2", computed: true])
        } else {
            logger.info("${MS_LEVEL} was already specified")
        }
        return true
    }

    /**
     * checks if we can accept this field
     * @param value
     * @return
     */
    protected boolean isCorrectMetaDataField(MetaDataValue value) {
        logger.debug("checking ${value.name} against defined field ${PRECURSORTYPE}")
        if (value.name.toLowerCase().equals(PRECURSORTYPE.toLowerCase())) {
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
        return "this rule utilizes the precursor type to add additional metadata to ensure spectra are marked correctly as MSMS"
    }

}
