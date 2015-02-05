package curation.actions
import curation.CurationAction
import curation.CurationObject
import grails.util.Holders
import moa.SupportsMetaData
import moa.server.tag.TagService
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 9/30/14
 * Time: 11:29 AM
 */
class AddTagAction implements CurationAction {

    @Autowired
    TagService tagService

    Logger logger = Logger.getLogger(getClass())

    String[] tagNameToAdd = null

    AddTagAction() {

    }

    /**
     * specify a tag for us
     * @param tagName
     */
    AddTagAction(String... tagName) {
        this()
        this.tagNameToAdd = tagName
    }


    @Override
    void doAction(CurationObject toValidate) {

        if(tagService == null){
            tagService = Holders.grailsApplication.mainContext.tagService
        }
        SupportsMetaData owner = null
        if (toValidate.isSpectra()) {
            owner = toValidate.getObjectAsSpectra()
        } else if (toValidate.isCompound()) {
            owner = toValidate.getObjectAsCompound()
        } else {
            throw new RuntimeException("not supported object: ${toValidate}")
        }

        logger.debug("adding tag(s) to (${owner.id} - ${tagNameToAdd}")
        if (!tagNameToAdd) {
            throw new RuntimeException("please provide us with a 'tagNameToAdd' value!")
        }

        for (String tag : tagNameToAdd) {
            tagService.addTagTo(tag, owner)

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

