package moa

class MetaData implements Comparable<MetaData> {

    static constraints = {
        name blank: false
        value nullable: true
        searchable nullable: true
        requiresUnit nullable: true
        priority nullable: true

    }

    static mapping = {
        name sqlType: "varchar(100)"
        version false
        priority defaultValue : 0

    }

    static belongsTo = [category: MetaDataCategory]

    static hasMany = [value: MetaDataValue]
    /**
     * the key for this metadata
     */
    String name

    /**
     * what kind of type we support
     */
    String type

    /**
     * the priority of this metadata object
     */
    Integer priority = 0

    /**
     * associated content
     */
    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof MetaData)) return false

        MetaData metaData = (MetaData) o

        if (requiresUnit != metaData.requiresUnit) return false
        if (searchable != metaData.searchable) return false
        if (category != metaData.category) return false
        if (name != metaData.name) return false
        if (type != metaData.type) return false

        return true
    }

    Set<MetaDataValue> value

    /**
     * is this metadata object searchable
     */
    boolean searchable = true

    /**
     * does this metadata object requires a unit or is the unit optional
     */
    boolean requiresUnit = false

    @Override
    int compareTo(MetaData metaData) {
        return this.priority.compareTo(metaData.priority)
    }

}
