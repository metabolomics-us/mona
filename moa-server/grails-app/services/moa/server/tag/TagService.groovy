package moa.server.tag

import grails.transaction.Transactional
import moa.SupportsMetaData
import moa.Tag

@Transactional
class TagService {

    /**
     * adds a tag to the given spectra
     * @param tagName
     * @param spectrum
     */
    def addTagTo(String tagName, SupportsMetaData meta) {

        SupportsMetaData.withNewSession {
            log.debug("adding tagName: ${tagName}")
            Tag tag = getTag(tagName)
            tag.lock()
            meta.addToTags(tag)
            tag.save(flush: true)
        }

    }

    /**
     * removes the given tag
     * @param tagName
     * @param meta
     */
    def removeTagFrom(String tagName, SupportsMetaData owner) {


        Tag tag = Tag.findOrSaveByText(it)
        tag.ruleBased = true
        tag.save(flush: true)

        if (owner.getTags().contains(tag)) {
            owner.removeFromTags(tag)
            tag.save(flush: true)
        } else {
            log.debug("spectra did not contain required tag!")
        }

    }

    /**
     * gets or creates a new tag
     * @param tagName
     * @return
     */
    Tag getTag(String tagName) {

        Tag tag = Tag.findByText(tagName)

        if (tag) {
            return tag
        } else {

            log.debug("creating new tag: ${tagName}")
            tag = new Tag(text: tagName)

            tag.save()


        }
        return tag
    }
}
