package curation.rules.tag
import curation.AbstractCurationRule
import curation.CurationObject
/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 10/14/14
 * Time: 8:40 AM
 */
class RemoveComputedTagRule extends AbstractCurationRule{
    @Override
    boolean executeRule(CurationObject toValidate) {

        def toRemove = []
        toValidate.objectAsSpectra.tags.each {
            if(it.ruleBased){
                toRemove.add(it)
            }
        }

        toRemove.each {
            logger.debug("removing tag(${it.text}) from spectra(${toValidate.objectAsSpectra.id})")
            toValidate.objectAsSpectra.removeFromTags(it)
        }

        return true
    }

    @Override
    boolean ruleAppliesToObject(CurationObject toValidate) {
        return toValidate.isSpectra()
    }
}
