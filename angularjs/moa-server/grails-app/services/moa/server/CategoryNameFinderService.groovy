package moa.server

import moa.MetaDataCategory

class CategoryNameFinderService {

    /**
     * finds a given category for a meta data key
     * @param metaDataKey
     * @return
     */
    def synchronized findCategoryForMetaDataKey(String metaDataKey, String providedCategoryName = null) {
	
        String name = ""

        if (providedCategoryName != null && providedCategoryName.length() > 0) {
            name = providedCategoryName;
        }
        name = MetaDataCategory.DEFAULT_CATEGORY_NAME

        MetaDataCategory category = MetaDataCategory.findOrSaveByName(name,[lock:true])
	category.refresh()
        category.save(flush:true)

        return category
    }
}
