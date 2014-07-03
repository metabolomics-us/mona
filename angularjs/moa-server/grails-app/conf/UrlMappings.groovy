import moa.MetaData

class UrlMappings {

    static mappings = {
        "/"(view: 'index')
        "/rest"(redirect: '/')
        "/rest/submitters"(resources: 'Submitter')
        "/rest/tags"(resources: 'Tag')
        "/rest/compounds"(resources: 'Compound') {
            "/spectra"(resources: 'Spectrum')
        }
        "/rest/spectra"(resources: 'Spectrum')

        "/rest/meta/data"(resources: 'MetaData')

        "/rest/meta/category"(resources: 'MetaDataCategory') {
            "/data"(resources: 'MetaData'){
                "/spectra"(resources: 'Spectrum')
                "/compounds"(resources: 'Compound')
                "/value"(resources: 'MetaDataValue')
            }
        }
    }
}
