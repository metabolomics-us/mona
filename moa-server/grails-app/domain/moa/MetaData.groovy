package moa

class MetaData implements Comparable<MetaData> {

    Date dateCreated
    Date lastUpdated

    static constraints = {
        name blank: false,unique:true
        value nullable: true
        searchable nullable: true
        requiresUnit nullable: true
        priority nullable: true
        hidden nullable: true
    }

    static mapping = {
        name sqlType: "varchar(100)"
        version false
        priority defaultValue : 0
        value  cascade: 'all-delete-orphan'

        //category fetch:'join'

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
    boolean searchable

    /**
     * does this metadata object requires a unit or is the unit optional
     */
    boolean requiresUnit

    /**
     * should this object be hidden
     */

    boolean hidden
    @Override
    int compareTo(MetaData metaData) {
        return this.priority.compareTo(metaData.priority)
    }

    @Override
    public String toString() {
        return "MetaData{" +
                "name='" + name + '\'' +
                ", category=" + category +
                '}';
    }
}
