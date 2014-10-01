package validation.actions

import moa.Spectrum
import moa.Tag
import org.apache.log4j.Logger
import validation.ValidationAction

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 10/1/14
 * Time: 12:25 PM
 */
class RemoveTagAction implements ValidationAction {

    Logger logger = Logger.getLogger(getClass())

    String tagNameToAdd = null

    RemoveTagAction() {

    }

    /**
     * specify a tag for us
     * @param tagName
     */
    RemoveTagAction(String tagName) {
        this.tagNameToAdd = tagName
    }


    @Override
    void doAction(Spectrum spectrum) {

        logger.debug("removing tag from spectrum(${spectrum.id} - ${tagNameToAdd})")
        if (!tagNameToAdd) {
            throw new RuntimeException("please provide us with a 'tagNameToAdd' value!")
        }

        Tag tag = Tag.findOrSaveByText(tagNameToAdd)
        tag.ruleBased = true
        tag.save(flush: true)

        if (spectrum.getTags().contains(tag)) {
            spectrum.removeFromTags(tag)
            tag.save(flush:true)
        }
        else{
            logger.warn("spectra did not contain required tag!")
        }


        spectrum.save(flush: true)

        logger.debug("=> done")
    }
}