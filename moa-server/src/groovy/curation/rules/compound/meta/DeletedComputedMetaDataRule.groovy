package curation.rules.compound.meta
import curation.AbstractCurationRule
import curation.CurationObject
import moa.MetaDataValue
import org.apache.log4j.Logger
/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 10/13/14
 * Time: 11:59 AM
 */
class DeletedComputedMetaDataRule extends AbstractCurationRule {

    private Logger logger = Logger.getLogger(getClass())

    @Override
    boolean executeRule(CurationObject toValidate) {

        def mowner
        if (toValidate.isCompound()) {
            mowner = toValidate.getObjectAsCompound()
        } else if (toValidate.isSpectra()) {
            mowner = toValidate.getObjectAsSpectra()
        }
        else{
            //should never happen, but let's no die over this
            return true
        }

        logger.info("running rule on: ${mowner}")


        MetaDataValue.where {
            (computed == true && owner == mowner)
        }.deleteAll()

        return true
    }

    @Override
    boolean ruleAppliesToObject(CurationObject toValidate) {
        return toValidate.isCompound() || toValidate.isSpectra()
    }
}