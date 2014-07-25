package moa

class MetaDataCategory {

    static final String DEFAULT_CATEGORY_NAME = "none"

    static constraints = {
        name blank: false ,unique: true
        metaDatas nullable: true
        visible nullable: true
    }

    static mapping = {
        version false
    }

    static hasMany = [metaDatas: MetaData]

    String name

    Set<MetaData> metaDatas

    /**
     * is this category object visible
     */
    boolean visible = true
}
