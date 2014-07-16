package moa.server

import moa.MetaData
import moa.MetaDataCategory

class CategoryNameFinderService {

    /**
     * finds a given category for a meta data key
     * @param metaDataKey
     * @return
     */
    def findCategoryNameForMetaDataKey(String metaDataKey) {
        MetaData data = MetaData.findByName(metaDataKey)

        if (data) {
            return data.category.name
        } else {
            return MetaDataCategory.DEFAULT_CATEGORY_NAME
        }
    }
}
