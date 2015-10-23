package moa

import event.MonaEvent

class Webhook {
    Date dateCreated
    Date lastUpdated

    static constraints = {
        submitter nullable: false
        monaEvent nullable: false
        targetUrl nullable: false
    }

    static belongsTo = [
            submitter:Submitter
    ]

    /**
     * owner of this webhook
     */
    Submitter submitter

    /**
     * which target url to invoke
     */
    String targetUrl

    /**
     * which event are we interested in
     */
    MonaEvent monaEvent

}
