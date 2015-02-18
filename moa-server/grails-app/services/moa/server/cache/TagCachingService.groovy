package moa.server.cache

import grails.transaction.Transactional
import moa.Tag
import org.springframework.cache.annotation.Cacheable

class TagCachingService {

    /**
     * computes how many spectra a tag ha
     * @param tag
     */
    @Cacheable("tag")
    @Transactional
    int computeSpectraCount(String text) {

        int spectraCount = 0
        Tag.withSession { session ->
            def result = session.createSQLQuery(" select count(*) as c from supports_meta_data_tag a, tag b where a.tag_id = b.id and b.text = ? group by text").setString(0, text).list()

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
    @Cacheable("tag")
    @Transactional
    int computeCompoundCount(String text) {

        int compoundCount = 0
        Tag.withSession { session ->
            def result = session.createSQLQuery(" select count(*) as c from supports_meta_data_tag a, tag b where a.tag_id = b.id and b.text = ? group by text").setString(0, text).list()

            if (!result.isEmpty()) {
                compoundCount = result[0]
            }

        }

        return compoundCount
    }

}
