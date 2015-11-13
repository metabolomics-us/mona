package moa.server.query

import grails.converters.JSON
import moa.Spectrum
import moa.SpectrumQueryDownload
import moa.server.convert.SpectraConversionService
import moa.server.mail.EmailService
import org.apache.commons.io.FileUtils
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Created by sajjan on 8/31/15.
 */
class SpectraQueryExportService {

    /**
     * access to the grails application configuration
     */
    GrailsApplication grailsApplication

    /**
     * service to query the backend
     */
    SpectraQueryService spectraQueryService

    /**
     * service to convert spectra objects to different formats
     */
    SpectraConversionService spectraConversionService

    /**
     * email service
     */
    EmailService emailService

    def exportQuery(def query, def emailAddress, def startTime) {
        log.info("Starting download job for " + emailAddress)

        // Get query as JSON
        def json = (query instanceof JSONObject) ? query : JSON.parse(query);

        // Create directory to store query export if needed
        String downloadPath = grailsApplication.config.queryDownloadDirectory

        File directory = new File(downloadPath)

        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new FileNotFoundException("was not able to create storage directory at: ${downloadPath}")
            }
        }

        // Determine output format
        String format = getFileFormat(json)

        // Create new download file object
        def label = "${emailAddress.split('@')[0]}-$startTime"
        def queryDownload = SpectrumQueryDownload.findOrCreateByLabelAndEmailAddress(label, emailAddress);

        if (!queryDownload.query) {
            queryDownload.query = query.toString();
            queryDownload.queryFile = "${downloadPath}/export-$label-query.json"
            queryDownload.exportFile = "${downloadPath}/export-$label.$format"
        }

        // Get the number of spectra in our query results
        def ids = spectraQueryService.queryForIds(json)
        log.info("Counted " + ids.size() + " spectra")

        // Export query to file
        File queryFile = new File(queryDownload.queryFile)
        log.debug("storing result at: ${queryFile.getAbsolutePath()}")
        if (!queryFile.exists()) {
            queryFile.createNewFile()
        }


        FileUtils.writeStringToFile(queryFile, json.toString())

        log.info("Exporting query file " + queryFile.getName())

        // Perform query in chunks and export data
        File exportFile = new File(queryDownload.exportFile)
        exportFile.createNewFile()

        log.info("Exporting data file ${exportFile.getName()} as $format")

        if (format == "json") {
            FileUtils.writeStringToFile(exportFile, "[\n", true)
        }

        int i = 0

        // Iterate over all queried spectra and export after converting to JSON or MSP format
        ids.each { def id ->
            Spectrum s = spectraQueryService.query(id.id)

            if (format == "json") {
                // Append comma and newline
                if (i > 0) {
                    FileUtils.writeStringToFile(exportFile, ",\n", true);
                }

                FileUtils.writeStringToFile(exportFile, (s as JSON).toString(), true);
            } else if (format == "msp") {
                FileUtils.writeStringToFile(exportFile, spectraConversionService.convertToMsp(s) + "\n", true);
            }

            i++;
            if (i % 1000 == 0) {
                log.info("\tExported $i spectra")
            }
        }

        if (format == "json") {
            FileUtils.writeStringToFile(exportFile, "\n]", true)
        }

        queryDownload.save(flush: true)

        // Email results
        log.info("Export of ${ids.size()} spectra complete, id ${queryDownload.id}, sending notification email to $emailAddress")
        emailService.sendDownloadEmail(emailAddress, ids.size(), queryDownload.id)
    }

    /**
     * TODO outsource this to be able to support more file, mzData, mzML, etc
     * @param json
     * @return
     */
    private String getFileFormat(json) {
        String format

        if (!json.format || json.format == "json") {
            format = "json"
        } else if (json.format == "msp") {
            format = "msp"
        } else {
            throw new FileNotFoundException("\t=>\tinvalid output format specified: " + json.format)
        }
        format
    }
}
