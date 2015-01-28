package curation.actions
import curation.CurationAction
import curation.CurationObject
import grails.util.Holders
import moa.server.tag.TagService
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

    TagService tagService
    RemoveTagAction() {
   }

    /**
     * specify a tag for us
     * @param tagName
     */
    RemoveTagAction(String... tagName) {
        this()
        this.tagNameToRemove = tagName
    }


    @Override
    void doAction(CurationObject toValidate) {

        if(tagService == null){
            tagService = Holders.grailsApplication.mainContext.tagService
        }

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

        tagNameToRemove.each { String name ->
            tagService.removeTagFrom(name,owner)
        }

        logger.debug("=> done")
    }


    @Override
    boolean actionAppliesToObject(CurationObject toValidate) {
        return (toValidate.isSpectra() || toValidate.isCompound())
    }

    @Override
    String getDescription() {
        return "this action will remove the specified tags from the given compound or spectra"
    }
}