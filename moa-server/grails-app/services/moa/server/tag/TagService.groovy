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

        log.debug("adding tagName: ${tagName}")
        Tag tag = Tag.findOrSaveByText(tagName, [lock: true])

        meta.addToTags(tag)
        tag.save()


    }

    /**
     * removes the given tag
     * @param tagName
     * @param meta
     */
    def removeTagFrom(String tagName, SupportsMetaData owner) {


        Tag tag = Tag.findOrSaveByText(tagName, [lock: true])

        if (owner != null && owner.getTags() != null) {
            if (owner.getTags().contains(tag)) {
                owner.removeFromTags(tag)
                tag.save()
                owner.save()
            } else {
                log.debug("spectra did not contain required tag!")
            }

        }
    }

}
