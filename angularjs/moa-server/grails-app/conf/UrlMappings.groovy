class UrlMappings {

    static mappings = {

        "/rest/submitters"(resources: 'Submitter')
        "/rest/tags"(resources: 'Tag')
        "/rest/compounds"(resources: 'Compound'){
            "/spectra"(resources: 'Spectrum')
        }
        "/rest/spectra"(resources: 'Spectrum')

        /**
         * converts a mol file to inchi
         */
        "/rest/util/converter/molToInchi"(controller: "molConverter", action: "moltoinchi")

        /**
         * converts an inchi key to a mol file
         */
        "/rest/util/converter/inchiKeyToMol"(controller: "molConverter", action: "inchiKeyToMol")

        /**
         * simples access to the cts service
         */
        "/rest/util/cts/inchiToName/$inchi"(controller: "CTS", action: "getNamesForInChIKey")

    }
}
