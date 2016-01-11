package curation.association

import curation.AbstractCurationRule
import curation.CurationObject

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 6/3/15
 * Time: 12:52 PM
 */
abstract class AbstractAssociationRule extends AbstractCurationRule {

    @Override
    protected boolean failByDefault() {
        return false
    }

    @Override
    boolean ruleAppliesToObject(CurationObject toValidate) {
        return toValidate.isSpectra()
    }
}
