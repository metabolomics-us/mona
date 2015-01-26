package moa.server

import grails.transaction.Transactional
import moa.Spectrum
import moa.Submitter

@Transactional
class SubmitterService {

    /**
     * finds or creates a submitter based on the given information
     */
    def findOrCreateSubmitter(Spectrum spectrum) {

        Submitter submitter = null

        submitter = Submitter.findByEmailAddress(spectrum.submitter.emailAddress)

        if (submitter) {
            log.debug("found existing submitter: ${submitter}")
            return submitter
        } else {
            log.debug("creating new submitter with ${spectrum.submitter.emailAddress}")
            submitter = spectrum.submitter
            submitter.password = "${System.currentTimeMillis()}"
            submitter.save()
        }

        return submitter
    }
}
