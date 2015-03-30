package curation.rules.meta

import curation.AbstractCurationRule
import curation.CurationObject
import moa.MetaDataValue
import moa.server.metadata.MetaDataPersistenceService

/**
 * drops not longer required metadata from the system
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 3/24/15
 * Time: 3:01 PM
 */
class DropNoneNecessaryMetaDataRule extends AbstractCurationRule {

    MetaDataPersistenceService metaDataPersistenceService

    /**
     * a bunch of values we don't need in the system
     */
    Collection<String> dataToDrop = []

    @Override
    boolean executeRule(CurationObject toValidate) {

        def toDrop = []
        toValidate.getObjectAsSpectra().metaData.each {MetaDataValue value ->

            dataToDrop.each {String valueName ->
                if(value.name.toLowerCase() == valueName.toLowerCase()){
                    toDrop.add(value)
                }
            }
        }

        toDrop.each {MetaDataValue v ->
            metaDataPersistenceService.removeMetaDataValue(v)
        }

        return true
    }

    @Override
    boolean ruleAppliesToObject(CurationObject toValidate) {
        return toValidate.isSpectra()
    }

    @Override
    String getDescription() {
        return "removes none required meta data values form the provided object."
    }
}
