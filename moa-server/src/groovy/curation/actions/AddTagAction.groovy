package curation.actions
import curation.CurationAction
import curation.CurationObject
import moa.Tag
import org.apache.log4j.Logger
/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 9/30/14
 * Time: 11:29 AM
 */
class AddTagAction implements CurationAction {

    Logger logger = Logger.getLogger(getClass())

    String[] tagNameToAdd = null

     AddTagAction(){

    }

    /**
     * specify a tag for us
     * @param tagName
     */
     AddTagAction(String... tagName){
        this.tagNameToAdd = tagName
    }


    @Override
    void doAction(CurationObject toValidate) {

        def owner = null
        if(toValidate.isSpectra()) {
            owner = toValidate.getObjectAsSpectra()
        }
        else if(toValidate.isCompound()){
            owner = toValidate.getObjectAsCompound()
        }
        else{
            throw new RuntimeException("not supported object: ${toValidate}")
        }

        logger.debug("adding tag(s) to (${owner.id} - ${tagNameToAdd}")
        if (!tagNameToAdd) {
            throw new RuntimeException("please provide us with a 'tagNameToAdd' value!")
        }

        tagNameToAdd.each {
            Tag tag = Tag.findOrSaveByText(it)
            tag.lock()
            tag.ruleBased = true
            tag.save(flush: true)

            owner.addToTags(tag)
            owner.save(flush: true)

            logger.debug("=> done")
        }
    }

    @Override
    boolean actionAppliesToObject(CurationObject toValidate) {
        return (toValidate.isSpectra() || toValidate.isCompound())
    }

    @Override
    String getDescription() {
        return "this action will add the defined actions to the compound or spectrum"
    }
}

