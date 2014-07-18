package moa


class MetaData {

    static constraints = {
        name blank: false
        value nullable: true
    }

    static mapping = {
        name sqlType: "varchar(100)"

    }

    static  belongsTo = [category:MetaDataCategory]

    static hasMany = [value:MetaDataValue]
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

}
