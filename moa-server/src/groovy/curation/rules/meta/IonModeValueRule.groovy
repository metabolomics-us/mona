package curation.rules.meta

import moa.server.metadata.MetaDataPersistenceService

import static util.MetaDataFieldNames.*

import curation.rules.AbstractMetaDataCentricRule
import moa.MetaDataValue
import org.apache.log4j.Logger

/**
 * Created by sajjan on 12/1/15.
 */
class IonModeValueRule extends AbstractMetaDataCentricRule {
    private Logger logger = Logger.getLogger(getClass())

    MetaDataPersistenceService metaDataPersistenceService

    def POSITIVE = ["positive", "pos", "p", "+"]
    def NEGATIVE = ["negative", "neg", "n", "-"]

    @Override
    protected boolean acceptMetaDataValue(MetaDataValue value) {
        String ionMode = value.value.toString()

        if (ionMode.toLowerCase() == "n/a") {
            logger.info("ion mode $ionMode is invalid, removing value")
            metaDataPersistenceService.removeMetaDataValue(value)
            return false
        } else if (ionMode == "positive" || ionMode == "negative") {
            return true
        } else {
            ionMode = ionMode.toLowerCase().trim()

            if (ionMode in POSITIVE) {
                logger.info("remapping $ionMode -> positive")

                metaDataPersistenceService.generateMetaDataObject(value.owner, [name: value.getName(), value: "positive", category: value.getCategory()])
                metaDataPersistenceService.removeMetaDataValue(value)

                return true
            } else if (ionMode in NEGATIVE) {
                logger.info("remapping $ionMode -> negative")

                metaDataPersistenceService.generateMetaDataObject(value.owner, [name: value.getName(), value: "negative", category: value.getCategory()])
                metaDataPersistenceService.removeMetaDataValue(value)

                return true
            } else {
                logger.info("invalid ion mode value: $ionMode")
                return false
            }
        }
    }

    protected boolean isCorrectMetaDataField(MetaDataValue field) {
        return field.name.toLowerCase() == ION_MODE.toLowerCase()
    }

    @Override
    String getDescription() {
        return "this rule remaps the $ION_MODE into a standard form"
    }
}