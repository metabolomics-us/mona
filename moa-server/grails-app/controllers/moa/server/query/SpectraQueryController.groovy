package moa.server.query

import grails.converters.JSON
import moa.Spectrum
import moa.auth.AuthenticationToken
import moa.server.DeleteSpectraJob
import moa.server.SpectraQueryDownloadJob
import moa.server.auth.AuthenticationService
import moa.server.convert.SpectraConversionService

class SpectraQueryController {

    static responseFormats = ['json']

    SpectraConversionService spectraConversionService

    AuthenticationService authenticationService


    /**
     * service to query the backend
     */
    SpectraQueryService spectraQueryService

    /**
     * search function for the query controller
     */
    def search() {
        def json = request.JSON

        def result = []
        if (json.query) {
            log.info("received query: " + json.query)
            result = spectraQueryService.query(json.query, params);
            json = json.query;
        } else {
            result = spectraQueryService.query(json, params);
        }

        switch (json.format) {
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
     * download function for query controller
     */
    def download() {
        def json = request.JSON
        def emailAddress = authenticationService.getSubmitterEmailAddressFromRequest(request)

        if (json.query) {
            log.info("received query download request: " + json.query +" from "+ emailAddress)
            SpectraQueryDownloadJob.triggerNow([query: json.query.toString(), emailAddress: emailAddress, max: params.max, offset: params.offset])
        } else {
            log.info("received query download request: " + json +" from "+ emailAddress)
            SpectraQueryDownloadJob.triggerNow([query: json.toString(), emailAddress: emailAddress, max: params.max, offset: params.offset])
        }

        render([message: "scheduling of download job successful, results will be emailed to "+ emailAddress] as JSON)
    }


    def countForSearch(){
        def json = request.JSON

        int result = 0
        if (json.query) {
            log.info("received query: " + json.query)
            result = spectraQueryService.getCountForQuery(json.query);
            json = json.query;
        } else {
            result = spectraQueryService.getCountForQuery(json);
        }

        render ([count:result] as JSON)
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

    /**
     * starts a batch delete against the server and removes
     * all the spectra associated with this query
     * this can take a couple of hours and fires off internal jobs
     */
    def searchAndDelete(){

        def json = request.JSON

        if(json.delete == null){
            render(status: 404, text: "please provide a 'delete' tag in your json payload. It has to respond to the standard MoNA query syntax");

        }
        else {

            log.info("received delete request: " + json.delete)

            DeleteSpectraJob.triggerNow([deleteSpectra:json.delete])

            render(['queued request for deletion'] as JSON)
        }
    }

    /**
     * runs a similarity search against the system
     */
    def similaritySearch() {

        def json = request.JSON

        log.info("received request: ${json}")
        if (json && json.spectra && json.minSimilarity) {

            if (json.maxHits == null) {
                json.maxHits = 10
            }

            if (json.commonIonCount == null) {
                json.commonIonCount = 3
            }

            log.info("modified request: ${json}")
            def id = null
            try {
                id = Long.parseLong(json.spectra.toString())
            }
            catch (NumberFormatException e) {
                id = json.spectra.toString()

            }

            long begin = System.currentTimeMillis()
            def result = spectraQueryService.findSimilarSpectraIds(id, json.minSimilarity as Double, json.commonIonCount as Integer, json.maxHits as Integer)


            def map = [result: result, statistics: [
                    duration: (System.currentTimeMillis() - begin)
            ], config        : json]

            render(map as JSON)
        } else {
            render(status: 404, text: "please provide a provide the following payLoade {'spectra:string or id',minSimilarity:0-1000,maxHits:0-25,commonIonCount:0-n'}");
        }

    }

}
