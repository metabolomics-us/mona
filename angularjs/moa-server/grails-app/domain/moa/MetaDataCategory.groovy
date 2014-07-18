package moa

class MetaDataCategory {

    static final String DEFAULT_CATEGORY_NAME = "none"

    static constraints = {
        name blank: false
        metaDatas nullable: true
    }

    static mapping = {
    }

    static hasMany = [metaDatas:MetaData]

    String name

    Set<MetaData> metaDatas
}
