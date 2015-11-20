package moa.server.query

import grails.converters.JSON
import moa.Spectrum
import moa.SpectrumQueryDownload
import moa.auth.AuthenticationToken
import moa.server.auth.AuthenticationService
import moa.server.convert.SpectraConversionService
import org.apache.commons.io.FileUtils
import util.FireJobs

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
            log.info("received params: " + params)

            result = spectraQueryService.query(json.query, params);
            json = json.query;
        } else {
            log.info("received query: " + json)
            log.info("received params: " + params)

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
     * export function for query controller
     */
    def export() {
        def json = request.JSON
        def emailAddress = authenticationService.getSubmitterEmailAddressFromRequest(request)

        if (json.query) {
            log.info("received query download request: " + json.query +" from "+ emailAddress)
            FireJobs.fireSpectraQueryExportJob([query: json.query.toString(), emailAddress: emailAddress])
        } else {
            log.info("received query download request: " + json +" from "+ emailAddress)
            FireJobs.fireSpectraQueryExportJob([query: json.toString(), emailAddress: emailAddress])
        }

        render([message: "scheduling of download job successful, results will be emailed to "+ emailAddress] as JSON)
    }

    /**
     * download function for query controller
     */
    def download() {
        def id = params.id
        def spectrumDownload = SpectrumQueryDownload.findById(id)

        if(!spectrumDownload) {
            render(text: "${id} is not a valid query export!")
        } else {
            File f = new File(spectrumDownload.exportFile);

            response.setHeader("Content-disposition", "attachment; filename=${f.getName()}")
            response.contentType = 'text/plain'
            response.outputStream << FileUtils.openInputStream(f)
            response.outputStream.flush()
        }
    }

    def downloadJson() {
        def id = params.id
        def spectrumDownload = SpectrumQueryDownload.findById(id)

        if(!spectrumDownload) {
            render(text: "${id} is not a valid query export!")
        } else {
            render(text: FileUtils.readFileToString(new File(spectrumDownload.queryFile), 'utf-8'))
        }
    }

    def countForSearch() {
        def json = request.JSON
        int result = 0

        if (json.query) {
            log.info("received query: " + json.query)
            result = spectraQueryService.getCountForQuery(json.query);
            json = json.query;
        } else {
            result = spectraQueryService.getCountForQuery(json);
        }

        render([count: result] as JSON)
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

            render(spectraQueryService.update(json.query, json.update) as JSON)
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

            log.info("modified request: ${json}")
            def id = null

            try {
                id = Long.parseLong(json.spectra.toString())
            } catch (NumberFormatException e) {
                id = json.spectra.toString()
            }

            long begin = System.currentTimeMillis()
            def result = spectraQueryService.findSimilarSpectraIds(id, json.minSimilarity as Double, json.maxHits as Integer)
            def map = [result: result, statistics: [duration: (System.currentTimeMillis() - begin)], config: json]

            render(map as JSON)
        } else {
            render(status: 404, text: "please provide a provide the following payLoade {'spectra:string or id',minSimilarity:0-1000,maxHits:0-25'}");
        }
    }
}
