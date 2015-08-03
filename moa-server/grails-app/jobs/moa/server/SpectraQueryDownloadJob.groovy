package moa.server

import grails.converters.JSON
import moa.Spectrum
import moa.server.convert.SpectraConversionService
import moa.server.mail.EmailService
import moa.server.query.SpectraQueryService
import org.apache.commons.io.FileUtils
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Created by sajjan on 7/28/15
 * Used to download large queries in the background
 */
class SpectraQueryDownloadJob {

    /**
     * do we automatically want to resubmit failed jobs
     */
    def resubmit = true

    /**
     * should this run concurrent over the whole cluster
     */
    def concurrent = true

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

    /**
     * needs to be defined
     */
    static triggers = {}


    def group = "download"

    def description = "downloads spectra data in the background of the server"


    def execute(context) {
        Map data = context.mergedJobDataMap

        log.error("STARTING")

        if (data != null) {
            if (data.containsKey('query') && data.containsKey('emailAddress')) {
                log.info("Starting download job for "+ data.emailAddress)

                long begin = System.currentTimeMillis()

                // Get query as JSON
                def json = (data.query instanceof JSONObject) ? data.query : JSON.parse(data.query)


                try {
                    // Create directory to store query export if needed
                    def downloadPath = grailsApplication.getConfig().queryDownloadDirectory + data.emailAddress +'/'
                    new File(downloadPath).mkdirs();


                    // Get the number of spectra in our query results
                    def queryCount = spectraQueryService.getCountForQuery(json)
                    log.info("Counted "+ queryCount +" spectra")


                    // Export query to file
                    def exportFilename = new Date().format('yyyyMMddHHmmss')

                    File queryFile = new File(downloadPath + exportFilename +'-query.json')
                    queryFile.createNewFile()
                    FileUtils.writeStringToFile(queryFile, data.query)

                    log.info("Exporting query file "+ queryFile.getName())


                    // Determine output format
                    String format = null

                    if (!json.format || json.format == "json") {
                        format = "json"
                    } else if (json.format == "msp") {
                        format = "msp"
                    } else {
                        log.info("\t=>\tinvalid output format specified: "+ json.format)
                        return
                    }


                    // Perform query in chunks and export data
                    File exportFile = new File(downloadPath + exportFilename +"."+ format)
                    exportFile.createNewFile()

                    log.info("Exporting data file "+ exportFile.getName() +" as "+ format)

                    if (format == "json") {
                        FileUtils.writeStringToFile(exportFile, "[\n", true)
                    }

                    def result = spectraQueryService.query(json)
                    int i = 0

                    // Iterate over all queried spectra and export after converting to JSON or MSP format
                    for (Spectrum s : result) {
                        if (format == "json") {
                            // Append comma and newline
                            if (i > 0) {
                                FileUtils.writeStringToFile(exportFile, ",\n", true);
                            }

                            FileUtils.writeStringToFile(exportFile, (s as JSON).toString(), true);
                        } else if (format == "msp") {
                            FileUtils.writeStringToFile(exportFile, spectraConversionService.convertToMsp(s) +"\n", true);
                        }

                        i++;
                        if (i % 1000 == 0) {
                            log.info("\tExported $i spectra")
                        }
                    }

                    if (format == "json") {
                        FileUtils.writeStringToFile(exportFile, "\n]", true)
                    }

                    // Email results
                    log.info("Export complete, sending notification email to "+ data.emailAddress)
                    emailService.sendDownloadEmail(data.emailAddress, queryCount.toString(), queryFile.getName(), exportFile.getName())
                } catch (Exception e) {
                    json.prettyPrint = true
                    log.debug(json, e)

                    if (resubmit) {
                        if (e instanceof IllegalArgumentException) {
                            log.error("fatal error - no resubmission possible", e)
                        } else {
                            log.error("resubmitting failed job to the system", e)
                            triggerNow([query: data.query, emailAddress: data.emailAddress])
                        }
                    } else {
                        log.error("download fatally failed: ${e.getMessage()}", e)
                    }
                }
            } else {
                log.info("\t=>\tno query/email address was provided!")
            }
        } else {
            log.info("\t=>\tno data was provided")
        }
    }
}
