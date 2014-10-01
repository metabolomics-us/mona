package moa

class Name {
    static mapping = {}

    static constraints = {
        name(maxSize: 1024)
    }

    static belongsTo = [compound:Compound]

    String name
}
