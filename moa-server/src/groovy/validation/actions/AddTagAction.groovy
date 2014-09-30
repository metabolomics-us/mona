package validation.actions

import moa.Spectrum
import moa.Tag
import org.apache.log4j.Logger
import validation.ValidationAction

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 9/30/14
 * Time: 11:29 AM
 */
class AddTagAction implements ValidationAction {

    Logger logger = Logger.getLogger(getClass())

    String tagNameToAdd = null

     AddTagAction(){

    }

    /**
     * specify a tag for us
     * @param tagName
     */
     AddTagAction(String tagName){
        this.tagNameToAdd = tagName
    }


    @Override
    void doAction(Spectrum spectrum) {

        logger.debug("adding tag to spectrum(${spectrum.id} - ${tagNameToAdd}")
        if (!tagNameToAdd) {
            throw new RuntimeException("please provide us with a 'tagNameToAdd' value!")
        }

        Tag tag = Tag.findOrSaveByText(tagNameToAdd)
        tag.ruleBased = true
        tag.save(flus:true)

        spectrum.addToTags(tag)
        spectrum.save(flush: true)

        logger.debug("=> done")
    }
}
