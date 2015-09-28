package moa.server.query

import grails.converters.JSON
import grails.transaction.Transactional
import moa.Spectrum
import moa.server.convert.SpectraConversionService
import util.FireJobs

@Transactional
class SpectraRepositoryService {

    def grailsApplication

    SpectraConversionService spectraConversionService

    /**
     * reports the spectra to the repository
     */
    def exportToRepository(Spectrum spectrum) {

        log.info("persisting spectrum: ${spectrum.id}")
        Date date = spectrum.getDateCreated()
        Calendar cal = Calendar.getInstance()
        cal.setTime(date)

        //where are we going to store our data
        File directory = new File("${grailsApplication.config.repository.directory}/${cal[Calendar.YEAR]}/${cal[Calendar.MONTH]}/${cal[Calendar.DAY_OF_MONTH]}/${spectrum.getBiologicalCompound().inchiKey}/${spectrum.getSplash().splash}/")

        log.info("storing at directory: ${directory}")
        if (!directory.exists()) {
            log.info("\t-> creating directory")
            if (!directory.mkdirs()) {
                throw new FileNotFoundException("sorry, was not able to create the required directory: ${directory.absolutePath}")
            }
        }

        //generating the actual json file
        File outputFile = new File(directory, "${spectrum.id}.json")

        if (outputFile.exists()) {
            log.info("\t-> overwriting existing file!")
        }
        persist(outputFile, spectrum)
    }

    /**
     * persisting the actual file somehow
     * @param outputFile
     * @param spectrum
     */
    private void persist(File outputFile, Spectrum spectrum) {
        BufferedWriter stream = new BufferedWriter(new FileWriter(outputFile))

        String content = (spectrum as JSON).toString(true)

        log.debug("serialized as: \n\n${content}")
        stream.write(content)
        stream.flush()
        stream.close()
    }

    /**
     * exports and created spectra in the last n days to the repository
     * @param days
     * @return
     */
    def exportCreatedToRepositoryFromLastNDays(int days = 7) {

        log.info("persisting all imported spectra of the last ${days} to the repository")
        Spectrum.executeQuery("select s.id from Spectrum s where s.dateCreated between ? and ?", [new Date() - days, (new Date())]).each { Long id ->

            //schedule a new job to parallize things a bit
            FireJobs.fireSpectraDumpJob([id: id])

        }

    }

}
