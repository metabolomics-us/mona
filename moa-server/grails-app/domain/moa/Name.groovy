package moa

class Name  {

    Date dateCreated
    Date lastUpdated

    static mapping = {
        name sqlType: "text"
        compound fetch: 'join'
    }

    static constraints = {
    }

    static belongsTo = [compound:Compound]

    String name
}
