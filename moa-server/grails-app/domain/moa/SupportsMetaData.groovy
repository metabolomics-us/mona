package moa

class SupportsMetaData {

    static constraints = {
    }

    static mapping = {
        tablePerSubclass true
        version false
        //tags  cascade: 'all-delete-orphan'
        metaData  cascade: 'all-delete-orphan'
    }

    Date dateCreated
    Date lastUpdated
    static hasMany = [metaData: MetaDataValue,tags:Tag]
}
