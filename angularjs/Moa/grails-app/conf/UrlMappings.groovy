class UrlMappings {

    static mappings = {

        "/rest/submitters"(resources: 'Submitter')

        "/"(view: "/index")

        "/rest/util/converter/molToInchi"(controller: "molConverter", action: "moltoinchi")
    }
}
