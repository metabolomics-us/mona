package moa


class MetaData {

    static constraints = {
        name blank: false
        value nullable: true
        searchable nullable: true
        requiresUnit nullable: true
    }

    static mapping = {
        name sqlType: "varchar(100)"
        version false

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
     * associated content
     */
    Set<MetaDataValue> value

    /**
     * is this metadata object searchable
     */
    boolean searchable = true

    /**
     * does this metadata object requires a unit or is the unit optional
     */
    boolean requiresUnit = false
}
