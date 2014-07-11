import moa.MetaData

class UrlMappings {

    static mappings = {
        "/"(view: 'index')
        "/rest"(redirect: '/')

        /**
         * query by submitter
         */
        "/rest/submitters"(resources: 'Submitter')     {
            /**
             * and spectra
             */
            "/spectra"(resources: 'Spectrum')
        }

        /**
         * query by tags
         */
        "/rest/tags"(resources: 'Tag'){
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
        "/rest/meta/data"(resources: 'MetaData'){
            //associated values
            "/value"(resources:'MetaDataValue')
        }

        /**
         * query by category
         */
        "/rest/meta/category"(resources: 'MetaDataCategory') {
            /**
             * and metadata
             */
            "/data"(resources: 'MetaData'){
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
        "/rest/spectra/searchAndUpdate"(controller: 'spectraQuery', action: 'searchAndUpdate')

    }
}
