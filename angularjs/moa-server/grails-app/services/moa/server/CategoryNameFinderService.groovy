package moa.server

import moa.MetaDataCategory

class CategoryNameFinderService {

    /**
     * finds a given category for a meta data key
     * @param metaDataKey
     * @return
     */
    def synchronized findCategoryNameForMetaDataKey(String metaDataKey, String providedCategoryName = null) {
        if(providedCategoryName != null && providedCategoryName.length() > 0){
            return providedCategoryName;
        }
       return MetaDataCategory.DEFAULT_CATEGORY_NAME
    }
}
