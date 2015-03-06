package moa

import grails.converters.JSON
import moa.server.QueueService

class QueueController {

    QueueService queueService

    def currentlyQueuedJobCount() {
        render([count: queueService.getAllJobs().size()] as JSON)
    }

    def currentlyRunningJobCount() {
        render([count: queueService.getRunningJobs().size()] as JSON)
    }

    def spectraWaitingForImportCount() {
        render([count: queueService.getJobs("upload").size()] as JSON)

    }

    def spectraWaitingForValidationCount() {
        render([count: queueService.getJobs("validation-spectra").size()] as JSON)

    }

    def compoundsWaitingForValidationCount() {
        render([count: queueService.getJobs("validation-compound").size()] as JSON)

    }


    def currentlyQueuedJob() {
        render(queueService.getAllJobs() as JSON)
    }

    def currentlyRunningJob() {
        render(queueService.getRunningJobs() as JSON)
    }

    def spectraWaitingForImport() {
        render(queueService.getJobs("upload") as JSON)

    }

    def spectraWaitingForValidation() {
        render(queueService.getJobs("validation-spectra") as JSON)

    }

    def compoundsWaitingForValidation() {
        render(queueService.getJobs("validation-compound") as JSON)

    }

    def jobs() {
        render(queueService.runningJobs as JSON)
    }


    def index() {}
}
