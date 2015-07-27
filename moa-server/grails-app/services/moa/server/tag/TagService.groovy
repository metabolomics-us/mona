package moa.server.tag

import grails.transaction.Transactional
import moa.SupportsMetaData
import moa.Tag
import moa.TagLink

@Transactional
class TagService {

    /**
     * adds a tag to the given spectra
     * @param tagName
     * @param spectrum
     */
    def addTagTo(String tagName, SupportsMetaData meta) {

        log.debug("adding tagName: ${tagName}")

        Tag tag = Tag.findOrSaveByText(tagName/*, [lock: true]*/)

        //avoids duplicated tagging
        if (TagLink.findByTagAndOwner(tag, meta) == null) {

            TagLink link = new TagLink()
            link.owner = meta
            link.tag = tag

            link.save()
        }

    }

    /**
     * removes the given tag
     * @param tagName
     * @param meta
     */
    def removeTagFrom(String tagName, SupportsMetaData owner) {


        Tag tag = Tag.findOrSaveByText(tagName/*, [lock: true]*/)

        def links = TagLink.findAllByOwnerAndTag(owner, tag);

        def toDelete = []

        links.each {
            toDelete.add(it)
        }

        toDelete.each {
            removeLink(it)
        }
    }

    def removeLink(TagLink link) {
        link.owner.removeFromLinks(link)
        link.tag.removeFromLinks(link)
        link.delete()
    }

}
