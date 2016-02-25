package moa.server.query

import grails.converters.JSON
import moa.Spectrum
import moa.SpectrumQueryDownload
import moa.query.Query
import moa.server.convert.SpectraConversionService
import moa.server.mail.EmailService
import org.apache.commons.io.FileUtils
import org.apache.tools.zip.ZipEntry
import org.apache.tools.zip.ZipOutputStream
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


    def exportQueryByLabel(def query, def label, def format) {
        log.info("Starting download job for $label")
        def queryDownload = exportQuery(query, label, format)

        // Assign query download object to the predefined query object
        def queryObject = Query.findByLabel(label)

        if (format == "msp") {
            queryObject.mspExport = queryDownload
        } else {
            queryObject.jsonExport = queryDownload
        }

        queryObject.save(flush: true)

        log.info("Export of spectra complete for $label, id ${queryDownload.id}")
    }


    def exportQueryByEmailAddress(def query, def emailAddress, def startTime, def format) {
        log.info("Starting download job for " + emailAddress)

        def label = "${emailAddress.split('@')[0]}-$startTime"
        def queryDownload = exportQuery(query, label, format)

        // Email results
        log.info("Export of spectra complete, id ${queryDownload.id}, sending notification email to $emailAddress")
        emailService.sendDownloadEmail(emailAddress, queryDownload.queryCount, queryDownload.id)
    }

    private SpectrumQueryDownload exportQuery(def query, def label, def format) {
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
        format = format ?: getFileFormat(json)

        // Create new download file object
        def queryDownload = SpectrumQueryDownload.findOrCreateByLabel("${label}-${format}");

        def queryFilename = "${downloadPath}/export-${label.replaceAll(' ', '_')}-query.json"
        def exportFilename = "${downloadPath}/export-${label.replaceAll(' ', '_')}.${format}"
        def compressedFilename = "${downloadPath}/export-${label.replaceAll(' ', '_')}-${format}.zip"

        if (!queryDownload.query) {
            queryDownload.query = query.toString();
            queryDownload.queryFile = queryFilename
            queryDownload.exportFile = compressedFilename
        }

        // Get the number of spectra in our query results
        def queryCount = spectraQueryService.getCountForQuery(json)
        queryDownload.queryCount = queryCount

        // Save query download object
        queryDownload.save(flush: true)
        queryDownload.errors.allErrors.each { println it }

        log.info("Counted $queryCount spectra")


        // Export query to file
        File queryFile = new File(queryFilename)
        File exportFile = new File(exportFilename)
        log.debug("storing result at: ${queryFile.getAbsolutePath()}")

        queryFile.createNewFile()
        FileUtils.writeStringToFile(queryFile, json.toString())


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
        }


        // Compress generated export
        File compressedFile = new File(compressedFilename)
        File compressedTemporaryFile = new File(compressedFilename +".tmp")

        log.info("Compressing ${exportFile.getName()} -> ${compressedFile.getName()}")

        ZipOutputStream zipFile = new ZipOutputStream(new FileOutputStream(compressedTemporaryFile))

        zipFile.withStream { zipOutputStream ->
            // Add file entry and write data
            log.info(exportFilename)
            zipOutputStream.putNextEntry(new ZipEntry(exportFile.getName()))

            // Stream with a defined buffer as exports may be very large
            new FileInputStream(exportFile).withStream { inputStream ->
                def buffer = new byte[1024]
                def length

                while((length = inputStream.read(buffer, 0, 1024)) > -1) {
                    zipOutputStream.write(buffer, 0, length)
                }
            }

            zipOutputStream.closeEntry()

            // Remove export file if arching is successful
            FileUtils.forceDelete(exportFile)
        }


        // Move temporary export file to stored location
        if(compressedFile.exists()) {
            FileUtils.forceDelete(compressedFile)
        }

        FileUtils.moveFile(compressedTemporaryFile, compressedFile)


        // Log filesize
        queryDownload.exportSize = compressedFile.length()
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
