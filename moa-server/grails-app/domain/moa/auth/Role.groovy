package moa.auth

/**
 * Created by sajjan on 2/23/15.
 */
class Role {
    String authority

    static mapping = {
        cache true
    }

    static constraints = {
        authority blank: false, unique: true
    }
}
