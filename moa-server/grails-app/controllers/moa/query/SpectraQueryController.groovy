package moa.query

import grails.converters.JSON
import moa.Spectrum
import moa.server.convert.SpectraConversionService
import moa.server.query.SpectraQueryService

class SpectraQueryController {

    static responseFormats = ['json']

    SpectraConversionService spectraConversionService

    /**
     * service to query the backend
     */
    SpectraQueryService spectraQueryService

    def beforeInterceptor = {
        log.info("$params - $request.JSON")
    }

    /**
     * search function for the query controller
     */
    def search() {

        def json = request.JSON

        def result = []
        if (json.query) {
            log.info("received query: " + json.query)
            result = spectraQueryService.query(json.query, params);
        } else {
            result = spectraQueryService.query(json, params);
        }

        switch (json.format ) {
            case "msp":
                for (Spectrum s : result) {
                    render spectraConversionService.convertToMsp(s)

                }
                break
            default:
                render(result as JSON)
        }
    }

    /**
     * runs the given search and executes a mass update against the database
     */
    def searchAndUpdate() {

        def json = request.JSON

        if (json.query == null) {
            render(status: 404, text: "please provide a 'query' tag in your json payload");

        } else if (json.update == null) {
            render(status: 404, text: "please provide a 'update' tag in your json payload, telling us what to update");

        } else {

            log.info("received query: " + json.query)
            log.info("received update payload: " + json.update)
            def result = spectraQueryService.update(json.query, json.update);

            render(result as JSON)
        }
    }

}
