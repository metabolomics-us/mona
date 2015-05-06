package moa.server

import grails.transaction.Transactional
import moa.MetaDataCategory

@Transactional
class CategoryNameFinderService {

    /**
     * finds a given category for a meta data key
     * @param metaDataKey
     * @return
     */
    MetaDataCategory findCategoryForMetaDataKey(String metaDataKey, String providedCategoryName = null) {
        MetaDataCategory category = null

        log.debug("trying to find best category for: '${metaDataKey}', user provided '${providedCategoryName}' as suggested category")
        String name = ""

        if (providedCategoryName != null && providedCategoryName.length() > 0) {
            name = providedCategoryName;
        } else {
            name = MetaDataCategory.DEFAULT_CATEGORY_NAME
        }

        category = MetaDataCategory.findByName(name, /*[lock: true]*/)

        //create a new category if it doesn't exit
        if (category == null) {
            category = new MetaDataCategory()
            category.name = name
            category.save()
        }
        log.debug("found category: '${category.id} - ${category.name}'")

        return category
    }
}
