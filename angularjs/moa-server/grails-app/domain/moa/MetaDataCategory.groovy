package moa

class MetaDataCategory {

    static constraints = {
        name blank: false
        metaDatas nullable: true
    }

    static mapping = {
        version false
    }

    static hasMany = [metaDatas:MetaData]

    String name

    Set<MetaData> metaDatas
}
