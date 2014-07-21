package moa.server

import moa.MetaDataCategory

class CategoryNameFinderService {

    /**
     * finds a given category for a meta data key
     * @param metaDataKey
     * @return
     */
    def synchronized findCategoryNameForMetaDataKey(String metaDataKey) {
       return MetaDataCategory.DEFAULT_CATEGORY_NAME
    }
}
