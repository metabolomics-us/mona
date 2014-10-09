package curation.actions

import curation.CurationObject
import moa.Spectrum
import moa.Tag
import org.apache.log4j.Logger
import curation.CurationAction

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 10/1/14
 * Time: 12:25 PM
 */
class RemoveTagAction implements CurationAction {

    Logger logger = Logger.getLogger(getClass())

    String tagNameToRemove = null

    RemoveTagAction() {

    }

    /**
     * specify a tag for us
     * @param tagName
     */
    RemoveTagAction(String tagName) {
        this.tagNameToRemove = tagName
    }


    @Override
    void doAction(CurationObject toValidate) {
        Spectrum spectrum = toValidate.getObjectAsSpectra()

        logger.debug("removing tag from spectrum(${spectrum.id} - ${tagNameToRemove})")
        if (!tagNameToRemove) {
            throw new RuntimeException("please provide us with a 'tagNameToRemove' value!")
        }

        Tag tag = Tag.findOrSaveByText(tagNameToRemove)
        tag.ruleBased = true
        tag.save(flush: true)

        if (spectrum.getTags().contains(tag)) {
            spectrum.removeFromTags(tag)
            tag.save(flush:true)
        }
        else{
            logger.debug("spectra did not contain required tag!")
        }


        spectrum.save(flush: true)

        logger.debug("=> done")
    }


    @Override
    boolean actionAppliesToObject(CurationObject toValidate) {
        return toValidate.isSpectra()
    }
}