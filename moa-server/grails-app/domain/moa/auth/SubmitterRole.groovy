package moa.auth

import moa.Submitter
import org.apache.commons.lang.builder.HashCodeBuilder

/**
 * Created by sajjan on 2/16/15.
 */
class SubmitterRole implements Serializable {
    private static final long serialVersionUID = -1;

    Submitter submitter
    Role role

    static mapping = {
        id composite: ['role', 'submitter']
        submitter lazy: false, fetch: 'join'
        role lazy: false, fetch: 'join'
        version false
    }

//    static constraints = {
//        role validator: { Role r, SubmitterRole sr ->
//            if (sr.submitter == null) {
//                return
//            }
//
//            SubmitterRole.withNewSession {
//                existing = SubmitterRole.exists(sr.submitter.id, r.id)
//            }
//
//            if (existing) {
//                return 'submitterRole.exists'
//            }
//        }
//    }


    boolean equals(other) {
        if (!(other instanceof SubmitterRole)) {
            return false
        }

        other.submitter?.is == submitter?.id && other.role?.id == role?.id
    }

    int hashCode() {
        def builder = new HashCodeBuilder()

        if (submitter) {
            builder.append(submitter.id)
        }
        if (role) {
            builder.append(role.id)
        }

        builder.toHashCode()
    }

    static SubmitterRole get(long submitterId, long roleId) {
        SubmitterRole.where {
            submitter == Submitter.load(submitterId) && role == Role.load(roleId)
        }.get()
    }

    static boolean exists(long submitterId, long roleId) {
        SubmitterRole.where {
            submitter == Submitter.load(submitterId) && role == Role.load(roleId)
        }.count() > 0
    }

    static SubmitterRole create(Submitter submitter, Role role, boolean flush = false) {
        def instance = new SubmitterRole(submitter: submitter, role: role)
        instance.save(flush: flush, insert: true)
        instance
    }

    static boolean remove(Submitter s, Role r) {
        if (s == null || r == null) {
            return false
        }

        int rowCount = SubmitterRole.where {
            submitter == Submitter.load(s.id) && role == Role.load(r.id)
        }.deleteAll()
    }

    static void removeAll(Submitter s) {
        if (s == null) {
            return
        }

        SubmitterRole.where {
            submitter == Submitter.load(s.id)
        }.deleteAll()
    }

    static void removeAll(Role r) {
        if (r == null) {
            return
        }

        SubmitterRole.where {
            role == Role.load(r.id)
        }.deleteAll()
    }
}
