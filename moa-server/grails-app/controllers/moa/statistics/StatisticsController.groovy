package moa.statistics

import grails.converters.JSON

/**
 * renders statistic in an easily consumable way
 */
class StatisticsController {

    def statisticsService

    def countOfSpectraForAllTags() {
        render statisticsService.getSpectraCountForAllTags() as JSON
    }

    def countOfSpectraForTag() {
        render statisticsService.getSpectraCountForTag(params.id)
    }

    def countOfCompoundsForTag() {
        render statisticsService.getCompoundCountForTag(params.id)
    }

    def metaDataValueCountForMetadataValueId() {
        render statisticsService.getSpectraCountForMetaDataId(params.id as long)   as JSON
    }

}
