package curation.actions

import curation.CurationObject
import moa.Spectrum
import moa.Tag
import org.apache.log4j.Logger
import curation.CurationAction

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

        Spectrum spectrum = toValidate.getObjectAsSpectra()

        logger.debug("adding tag(s) to spectrum(${spectrum.id} - ${tagNameToAdd}")
        if (!tagNameToAdd) {
            throw new RuntimeException("please provide us with a 'tagNameToAdd' value!")
        }

        tagNameToAdd.each {
            Tag tag = Tag.findOrSaveByText(it)
            tag.ruleBased = true
            tag.save(flus: true)

            spectrum.addToTags(tag)
            spectrum.save(flush: true)

            logger.debug("=> done")
        }
    }

    @Override
    boolean actionAppliesToObject(CurationObject toValidate) {
        return toValidate.isSpectra()
    }
}
