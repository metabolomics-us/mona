class UrlMappings {

    static mappings = {

        /**
         * provides us with the submiter resource to persist it
         */
        "/rest/submitters"(resources: 'Submitter')

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
