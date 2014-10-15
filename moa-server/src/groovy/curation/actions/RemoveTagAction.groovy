package curation.actions
import curation.CurationAction
import curation.CurationObject
import moa.Tag
import org.apache.log4j.Logger
/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 10/1/14
 * Time: 12:25 PM
 */
class RemoveTagAction implements CurationAction {

    Logger logger = Logger.getLogger(getClass())

    String[] tagNameToRemove = null

    RemoveTagAction() {

    }

    /**
     * specify a tag for us
     * @param tagName
     */
    RemoveTagAction(String... tagName) {
        this.tagNameToRemove = tagName
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

        logger.debug("removing tag from spectrum(${owner.id} - ${tagNameToRemove})")
        if (!tagNameToRemove) {
            throw new RuntimeException("please provide us with a 'tagNameToRemove' value!")
        }

        tagNameToRemove.each {
            Tag tag = Tag.findOrSaveByText(it)
            tag.ruleBased = true
            tag.save(flush: true)

            if (owner.getTags().contains(tag)) {
                owner.removeFromTags(tag)
                tag.save(flush: true)
            } else {
                logger.debug("spectra did not contain required tag!")
            }

        }

        owner.save(flush: true)

        logger.debug("=> done")
    }


    @Override
    boolean actionAppliesToObject(CurationObject toValidate) {
        return (toValidate.isSpectra() || toValidate.isCompound())
    }
}