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

    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof MetaDataCategory)) return false

        MetaDataCategory that = (MetaDataCategory) o

        if (visible != that.visible) return false
        if (name != that.name) return false

        return true
    }

    static hasMany = [metaDatas: MetaData]

    String name

    Set<MetaData> metaDatas

    /**
     * is this category object visible
     */
    boolean visible = true

}
