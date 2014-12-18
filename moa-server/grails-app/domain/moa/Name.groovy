package moa

class Name  {

    Date dateCreated
    Date lastUpdated

    static mapping = {}

    static constraints = {
        name(maxSize: 1024)
    }

    static belongsTo = [compound:Compound]

    String name
}
