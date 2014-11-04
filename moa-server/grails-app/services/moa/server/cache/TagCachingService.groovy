package moa.server.cache

import grails.transaction.Transactional
import moa.Tag

@Transactional
class TagCachingService {

    /**
     * computes how many spectra a tag ha
     * @param tag
     */
    int computeSpectraCount(String text) {

        int spectraCount = 0
        Tag.withSession { session ->
            def result = session.createSQLQuery(" select count(*) as c from spectrum_tag a, tag b where a.tag_id = b.id and b.text = ? group by text").setString(0, text).list()

            if (!result.isEmpty()) {
                spectraCount = result[0]
            }
        }

        return spectraCount
    }

    /**
     * computes how many compounds a tag has
     * @param text
     * @return
     */
    int computeCompoundCount(String text) {

        int compoundCount = 0
        Tag.withSession { session ->
            def result = session.createSQLQuery(" select count(*) as c from compound_tag a, tag b where a.tag_id = b.id and b.text = ? group by text").setString(0, text).list()

            if (!result.isEmpty()) {
                compoundCount = result[0]
            }

        }

        return compoundCount
    }

}
