package curation.rules.compound.meta
import curation.AbstractCurationRule
import curation.CurationObject
import moa.Compound
import moa.MetaDataValue
import org.apache.log4j.Logger
/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 10/13/14
 * Time: 11:59 AM
 */
class DeletedComputedMetaDataRule extends AbstractCurationRule{

    private Logger logger = Logger.getLogger(getClass())

    @Override
    boolean executeRule(CurationObject toValidate) {

        Compound compound = toValidate.objectAsCompound

        logger.info("running rule on: ${compound}")


        MetaDataValue.where{
            (computed == true && owner == compound)
        }.deleteAll()

       return true
    }

    @Override
    boolean ruleAppliesToObject(CurationObject toValidate) {
        return toValidate.isCompound()
    }
}