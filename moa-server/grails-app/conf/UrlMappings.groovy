import moa.MetaData

class UrlMappings {

    static mappings = {
        /**
         * basic overview of our services
         */
        "/"(view: 'index')

        /**
         * status of our job queue
         */

        "/info/jobs"(controller: "Quartz", action: "list")

        /**
         * general grails default mapping
         */
        "/$controller/$action?/$id?"{
            constraints {
                // apply constraints here
            }
        }

        /**
         * if you hit the top directory just redirect
         */
        "/rest"(redirect: '/')

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
        "/rest/spectra"(resources: 'Spectrum')

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
         * provides us with access to simple queries
         */
        "/rest/spectra/search"(controller: 'spectraQuery', action: 'search')
        /**
         * batch updates based on queries
         */
        "/rest/spectra/batch/update"(controller: 'spectraQuery', action: 'searchAndUpdate')

        /**
         * batch save method, to schedule lots of spectra to an internal queue.
         */
        "/rest/spectra/batch/save"(controller: 'spectrum', action: 'batchSave')


    }
}
