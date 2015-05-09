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

        def object

        if(toValidate.isSpectra()){
            object = toValidate.objectAsSpectra
        }
        else if(toValidate.isCompound()){
            object = toValidate.objectAsCompound
        }

       object.links.each {
            if(it.tag.ruleBased){
                toRemove.add(it)
            }
        }

        toRemove.each {
            logger.debug("removing tag(${it.text}) from (${object.id})")
            object.removeFromTags(it)

            tagService.removeLink(it)
        }


        return true
    }

    @Override
    boolean ruleAppliesToObject(CurationObject toValidate) {
        return (toValidate.isSpectra() || toValidate.isCompound())
    }

    @Override
    String getDescription() {
        return "this rule removes all automatically computed tags from a compound or a spectra"
    }
}
