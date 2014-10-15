package curation.rules.compound.meta

import curation.AbstractCurationRule
import curation.CurationObject
import moa.Compound
import moa.MetaDataValue
import moa.server.caluclation.CompoundPropertyService
import org.apache.log4j.Logger

/**
 *
 * computes general useful metadata for compound
 * User: wohlgemuth
 * Date: 10/13/14
 * Time: 10:46 AM
 */
class CompoundComputeMetaDataRule extends AbstractCurationRule{

    CompoundPropertyService compoundPropertyService

    private Logger logger = Logger.getLogger(getClass())

    @Override
    boolean executeRule(CurationObject toValidate) {

        Compound compound = toValidate.objectAsCompound

        logger.info("running rule on: ${compound}")

        compoundPropertyService.calculateMetaData(compound)
        //drop all the computed metadata for this compound
        compound.getMetaData().each {MetaDataValue value ->

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
        return "this rule calculates common metadata fields for a given compound"
    }
}
