package moa.server.query

import grails.converters.JSON
import moa.Spectrum
import moa.SpectrumQueryDownload
import moa.query.Query
import moa.server.convert.SpectraConversionService
import moa.server.mail.EmailService
import org.apache.commons.io.FileUtils
import org.apache.ivy.util.FileUtil
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.web.json.JSONObject
import org.hibernate.SessionFactory

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


    SessionFactory sessionFactory


    int QUERY_SIZE = 100


    def exportQueryByLabel(def query, def label) {
        log.info("Starting download job for $label")
        def queryDownload = exportQuery(query, label)

        def queryObject = Query.findByLabel(label)
        queryObject.queryExport = queryDownload
        queryObject.save(flush: true)

        log.info("Export of spectra complete for $label, id ${queryDownload.id}")
    }


    def exportQueryByEmailAddress(def query, def emailAddress, def startTime) {
        log.info("Starting download job for " + emailAddress)

        def label = "${emailAddress.split('@')[0]}-$startTime"
        def queryDownload = exportQuery(query, label)


        // Email results
        log.info("Export of spectra complete, id ${queryDownload.id}, sending notification email to $emailAddress")
        emailService.sendDownloadEmail(emailAddress, queryDownload.queryCount, queryDownload.id)
    }

    private SpectrumQueryDownload exportQuery(def query, def label) {
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
        def queryDownload = SpectrumQueryDownload.findOrCreateByLabel(label);

        if (!queryDownload.query) {
            queryDownload.query = query.toString();
            queryDownload.queryFile = "${downloadPath}/export-$label-query.json"
            queryDownload.exportFile = "${downloadPath}/export-$label.$format"
        }

        // Get the number of spectra in our query results
        def queryCount = spectraQueryService.getCountForQuery(json)
        queryDownload.queryCount = queryCount

        log.info("Counted $queryCount spectra")


        // Export query to file
        File queryFile = new File(queryDownload.queryFile)
        log.debug("storing result at: ${queryFile.getAbsolutePath()}")

        boolean moveExportFile = false
        File exportFile

        if (queryFile.exists()) {
            log.info("Query file ${queryFile.getName()} exists, creating temporary dump file")

            exportFile = new File(queryDownload.exportFile +".tmp")
            moveExportFile = true
        } else {
            log.info("Exporting query file " + queryFile.getName())

            queryFile.createNewFile()
            FileUtils.writeStringToFile(queryFile, json.toString())

            exportFile = new File(queryDownload.exportFile)
        }


        // Perform query in chunks and export data
        exportFile.createNewFile()

        log.info("Exporting data file ${exportFile.getName()} as $format")


        // Iterate over all queried spectra and export after converting to JSON or MSP format
        exportFile.withWriter { writer ->
            if (format == "json") {
                writer.append("[\n")
            }

            boolean firstSpectrum = true;

            for (int i = 0; i < queryCount; i += QUERY_SIZE) {
                def result = spectraQueryService.query(json, QUERY_SIZE, i)

                for (Spectrum s : result) {
                    if (format == "json") {
                        // Append comma and newline
                        if (!firstSpectrum) {
                            writer.append(",\n")
                        } else {
                            firstSpectrum = false;
                        }

                        writer.append((s as JSON).toString())
                    } else if (format == "msp") {
                        writer.append(spectraConversionService.convertToMsp(s))
                        writer.append('\n')
                    }
                }

                sessionFactory.currentSession.flush()
                sessionFactory.currentSession.clear()

                log.info("Exported ${Math.min(i + QUERY_SIZE, queryCount)} / $queryCount for $label")
            }

            if (format == "json") {
                writer.append("\n]")
            }

            return
        }


        // Move temporary export file to stored location
        if(moveExportFile) {
            File toFile = new File(queryDownload.exportFile)
            FileUtils.forceDelete(toFile)
            FileUtils.moveFile(exportFile, toFile)
        }

        queryDownload.save(flush: true)
        queryDownload.errors.allErrors.each { println it }

        return queryDownload
    }

    /**
     * @param json
     * @return
     */
    private String getFileFormat(def json) {
        String format

        if (!json.format || json.format == "json") {
            format = "json"
        } else if (json.format == "msp") {
            format = "msp"
        } else {
            throw new FileNotFoundException("\t=>\tinvalid output format specified: ${json.format}")
        }

        return format
    }
}
