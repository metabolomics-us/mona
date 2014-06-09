class UrlMappings {

    static mappings = {

        /**
         * provides us with the submiter resource to persist it
         */
        "/rest/submitters"(resources: 'Submitter')

        /**
         * a simple rest converter for testing
         */
        "/rest/util/converter/molToInchi"(controller: "molConverter", action: "moltoinchi")

        /**
         * simples access to the cts service
         */
        "/rest/util/cts/inchiToName/$inchi"(controller: "CTS", action: "getNamesForInChIKey")

    }
}
