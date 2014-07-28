package moa

class SupportsMetaData {

    static constraints = {
    }

    static mapping = {
        tablePerSubclass true
        version false
    }

    Date dateCreated
    Date lastUpdated

    static hasMany = [metaData: MetaDataValue]
}
