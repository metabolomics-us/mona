package moa.server
import grails.transaction.Transactional
import moa.Submitter

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

            submitter.password = "${System.currentTimeMillis()}"
            log.debug("valid: ${submitter.validate()} - ${submitter.errors}" )
            submitter.save(flush:true)
        }

        return submitter
    }
}
