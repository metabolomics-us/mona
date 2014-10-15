package curation.rules.compound
import curation.AbstractCurationRule
import curation.CurationAction
import curation.CurationObject
/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 10/14/14
 * Time: 12:13 PM
 */
abstract class AbstractCompoundRule extends AbstractCurationRule{
    @Override
    boolean ruleAppliesToObject(CurationObject toValidate) {
        return toValidate.isCompound()
    }

    AbstractCompoundRule(CurationAction successAction, CurationAction failureAction) {
        super(successAction, failureAction)
    }

    AbstractCompoundRule() {
    }


}
