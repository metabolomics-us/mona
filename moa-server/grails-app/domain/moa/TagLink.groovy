package moa

class TagLink {

    static constraints = {
    }

    Tag tag

    SupportsMetaData owner

    static mapping = {
        version false
        tag fetch: 'join'
        owner fetch: 'join'

    }
}
