import moa.MetaData

class UrlMappings {

    static mappings = {
        /**
         * basic overview of our services
         */
        "/"(redirect: '/documentation')

        "/documentation"(controller: 'documentation', action: 'index')

        /**
         * if you hit the top directory just redirect
         */
        "/rest"(redirect: '/documentation')

        /**
         * query by submitter
         */
        "/rest/submitters"(resources: 'Submitter') {
            /**
             * and spectra
             */
            "/spectra"(resources: 'Spectrum')
        }

        /**
         * query by tags
         */
        "/rest/tags"(resources: 'Tag') {
            /**
             * and spectra
             */
            "/spectra"(resources: 'Spectrum')
        }

        /**
         * query by compounds
         */
        "/rest/compounds"(resources: 'Compound') {
            /**
             * and spectra
             */
            "/spectra"(resources: 'Spectrum')
        }

        /**
         * renders a compound with this id as mol file
         */
        "/rest/compounds/$id/mol"(controller: 'molRender', action: 'renderCompoundAsMolFile', id: id)

        /**
         * curates a single compound
         */
        "/rest/compounds/curate/$id"(controller: 'compoundCuration', action: 'curate', id: id)

        /**
         * curates all compounds
         */
        "/rest/compounds/curateAll"(controller: 'compoundCuration', action: 'curateAll')

        /**
         * query all the metadata without category
         */
        "/rest/meta/data"(resources: 'MetaData') {
            //associated values
            "/value"(resources: 'MetaDataValue')
        }

        /**
         * query all metadata by a given search term and category
         */
        "/rest/meta/data/search"(controller: 'metaDataQuery', action: 'query')

        /**
         * query by category
         */
        "/rest/meta/category"(resources: 'MetaDataCategory') {
            /**
             * and metadata
             */
            "/data"(resources: 'MetaData') {
                /**
                 * and spectra
                 */
                "/spectra"(resources: 'Spectrum')
                /**
                 * and compound
                 */
                "/compounds"(resources: 'Compound')
                /**
                 * and value
                 */
                "/value"(resources: 'MetaDataValue')
            }
        }

        /**
         * spectra based stuff
         */
        "/rest/spectra"(resources: 'Spectrum')

        /**
         * provides us with access to simple queries
         */
        "/rest/spectra/search"(controller: 'spectraQuery', action: 'search')

        "/rest/spectra/similarity/"(controller: 'spectraQuery', action: 'similaritySearch')

        /**
         * batch updates based on queries
         */
        "/rest/spectra/batch/update"(controller: 'spectraQuery', action: 'searchAndUpdate')

        /**
         * batch save method, to schedule lots of spectra to an internal queue.
         */
        "/rest/spectra/batch/save"(controller: 'spectrum', action: 'batchSave')

        "/rest/spectra/upload"(controller: 'spectrum', action: 'upload')


        /**
         * delete all of these data
         */
        "/rest/spectra/batch/delete"(controller: 'spectraQuery', action: 'searchAndDelete')

        /**
         * batch save method, to schedule lots of spectra to an internal queue.
         */
        "/rest/spectra/single/save"(controller: 'spectrum', action: 'singleSave', parseRequest: false)

        /**
         * curation services
         */
        "/rest/spectra/curate/$id"(controller: 'spectraCuration', action: 'curate', id: id)

        "/rest/spectra/curateNow/$id"(controller: 'spectraCuration', action: 'curateNow', id: id)

        /**
         * curation services
         */
        "/rest/spectra/curateAll"(controller: 'spectraCuration', action: 'curateAll')

        "/rest/spectra/curateByQuery"(controller: 'spectraCuration', action: 'curateByQuery')

        /**
         * scoring service
         */
        "/rest/spectra/score/$id"(controller: 'scoring',action: 'score',id:id)
        "/rest/spectra/score/$id/explain"(controller: 'scoring',action: 'scoreExplain',id:id)


        /**
         * general limited public api
         */

        //limited access to our available tags to be integrated into external api's
        "/rest/limited/list/tags"(controller: 'Tag', action: 'listPublic')

        /**
         * statistics api
         */
        "/rest/statistics/countAll"(controller: 'statistics', action: 'countAll')

        "/rest/statistics/tags/spectra/count/$id"(controller: 'statistics', action: 'countOfSpectraForTag', id: id)

        "/rest/statistics/tags/spectra/countAll"(controller: 'statistics', action: 'countOfSpectraForAllTags')

        "/rest/statistics/submitters/countAll"(controller: 'statistics', action: 'countOfSpectraForAllSubmitters')

        "/rest/statistics/tags/compound/count/$id"(controller: 'statistics', action: 'countOfCompoundsForTag', id: id)

        "/rest/statistics/meta/spectra/count/$id"(controller: 'statistics', action: 'metaDataValueCountForMetadataValueId', id: id)

        "/rest/statistics/category/$category/$grouping?"(controller: 'statistics', action: 'statisticsForCategory')

        "/rest/statistics/jobs/pending"(controller: 'statistics', action: 'statisticsForPendingJobs')

        /**
         * news related items
         */
        "/rest/news"(resources: 'News')

        "/rest/news/query/announcements"(controller: 'news', action: 'listAnnouncements')

        "/rest/news/query/notifications"(controller: 'news', action: 'listNotifications')

        "/rest/news/query/uploads"(controller: 'news',
                action: 'listUploads')
        "/rest/news/query/milestones"(controller: 'news',
                action: 'listMilestones')

        /**
         * queue related items
         */

        "/rest/queue/spectra/validation"(controller: 'queue', action: 'spectraWaitingForValidation')
        "/rest/queue/spectra/validation/count"(controller: 'queue', action: 'spectraWaitingForValidationCount')

        "/rest/queue/spectra/import"(controller: 'queue', action: 'spectraWaitingForImport')
        "/rest/queue/spectra/import/count"(controller: 'queue', action: 'spectraWaitingForImportCount')

        "/rest/queue/compound/validation"(controller: 'queue', action: 'compoundsWaitingForValidation')
        "/rest/queue/compound/validation/count"(controller: 'queue', action: 'compoundsWaitingForValidationCount')

        "/rest/queue"(controller: 'queue', action: 'jobs')

        /**
         * error related parts
         */
        "500"(controller: "error", action: "handle500")
        "404"(controller: "error", action: "handle404")

    }
}
