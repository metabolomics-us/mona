package moa.server

import moa.MetaDataCategory

class CategoryNameFinderService {

    /**
     * finds a given category for a meta data key
     * @param metaDataKey
     * @return
     */
    def synchronized findCategoryForMetaDataKey(String metaDataKey, String providedCategoryName = null) {
        log.debug("trying to find best category for: '${metaDataKey}', user provided '${providedCategoryName}' as suggested category")
        String name = ""

        if (providedCategoryName != null && providedCategoryName.length() > 0) {
            name = providedCategoryName;
        } else {
            name = MetaDataCategory.DEFAULT_CATEGORY_NAME
        }

        MetaDataCategory category = MetaDataCategory.findOrSaveByName(name)

//        category.refresh()
//        category.save(flush: true)

        log.debug("found category: '${category.id} - ${category.name}'")
        return category
    }
}
