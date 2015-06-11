package moa.statistics

import grails.converters.JSON

/**
 * renders statistic in an easily consumable way
 */
class StatisticsController {

    def statisticsService

    def countAll() {
        render statisticsService.countAll() as JSON
    }

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
        render statisticsService.getSpectraCountForMetaDataId(params.id as long) as JSON
    }

    def countOfSpectraForAllSubmitters() {
        render statisticsService.getSpectraCountForAllSubmitters() as JSON
    }

    def countOfSpectraForSubmitter(){
        render ([count:statisticsService.getSpectraCountForSubmitter(params.id as long)] as JSON )
    }

    def scoringStatisticsBySubmitters(){
        render statisticsService.getSpectraScoringBySubmitters() as JSON
    }

    def qualityOfSpectraForSubmitter(){
        render statisticsService.getSpectraQualityForSubmitter(params.id as long) as JSON
    }


    def statisticsForPendingJobs(){
        render statisticsService.getCountForPendingJobs() as JSON
    }
    def statisticsForCategory() {

        if (params.grouping == null) {
            render statisticsService.getStatisticsForCategory(params.category) as JSON
        } else {
            render statisticsService.getStatisticsForCategory(params.category,params.grouping) as JSON

        }
    }

}
