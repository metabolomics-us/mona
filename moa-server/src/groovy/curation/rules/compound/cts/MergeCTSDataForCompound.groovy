package curation.rules.compound.cts

import curation.AbstractCurationRule
import curation.CurationObject
import moa.Compound
import moa.MetaDataValue
import moa.server.caluclation.CompoundPropertyService
import org.apache.log4j.Logger

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 8/13/15
 * Time: 2:39 PM
 */
class MergeCTSDataForCompound extends AbstractCurationRule{

    CompoundPropertyService compoundPropertyService

    private Logger logger = Logger.getLogger(getClass())

    @Override
    boolean executeRule(CurationObject toValidate) {

        Compound compound = toValidate.objectAsCompound

        logger.info("running rule on: ${compound}")

        compoundPropertyService.calculateMetaData(compound)
        //drop all the computed metadata for this compound
        compound.listAvailableValues().each {MetaDataValue value ->

            logger.info("${value.getCategory()} - ${value.getName()} - ${value.getValue()} - ${value.isComputed()}}")
        }
        return true
    }

    @Override
    boolean ruleAppliesToObject(CurationObject toValidate) {
        return toValidate.isCompound()
    }


    @Override
    String getDescription() {
        return "this rule integrates all the cts, provided metadata values"
    }
}
