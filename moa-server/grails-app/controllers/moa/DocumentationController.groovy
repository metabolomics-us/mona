package moa

import grails.converters.JSON

/**
 * generates a basic overview of all our available services
 */
class DocumentationController {

    static responseFormats = ['json']

    def grailsApplication
    def grailsUrlMappingsHolder

    def index() {
        def urlMappings = new HashSet()

        grailsUrlMappingsHolder.urlMappings.each {
            String uri = it.urlData.logicalUrls.first()

            if (uri.startsWith("/rest/") &&
                    !uri.endsWith("/create") &&
                    !uri.endsWith("/edit") &&
                    !uri.endsWith("/edit")
            ) {

                def object = [:]


                object.url = it.urlData.logicalUrls.first()
                object.urlParameters = it.constraints.propertyName
                object.httpMethod = it.httpMethod

                urlMappings.add(
                        object
                )

            }

        }

        render urlMappings as JSON
    }
}
