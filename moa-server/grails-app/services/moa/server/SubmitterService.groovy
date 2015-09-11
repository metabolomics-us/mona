package moa.server
import grails.transaction.Transactional
import moa.Submitter
import moa.auth.Role
import moa.auth.SubmitterRole

@Transactional
class SubmitterService {

    /**
     * finds or creates a submitter based on the given information
     */
    def findOrCreateSubmitter(Map json) {
        Submitter submitter = Submitter.findByEmailAddress(json.emailAddress)

        if (submitter) {
            log.debug("found existing submitter: ${submitter}")
            return submitter
        } else {
            log.debug("creating new submitter with ${json.emailAddress}")

            submitter = new Submitter()
            submitter.emailAddress = json.emailAddress
            submitter.firstName = json.firstName ?: "not provided"
            submitter.lastName = json.lastName ?: "not provided"
            submitter.institution = json.institution
            submitter.password = "${System.currentTimeMillis()}"
            submitter.save(flush:true)
        }

        return submitter
    }
}
