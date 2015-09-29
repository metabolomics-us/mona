package moa.server.query

import grails.converters.JSON
import grails.transaction.Transactional
import moa.Spectrum
import moa.server.convert.SpectraConversionService
import moa.server.statistics.StatisticsService
import util.FireJobs

@Transactional
class SpectraRepositoryService {

    def grailsApplication

    StatisticsService statisticsService

    /**
     * reports the spectra to the repository
     */
    def exportToRepository(Spectrum spectrum) {

        if(spectrum.splash == null){
            log.info("spectra wasn't curated yet, requesting curation!")
            FireJobs.fireSpectraCurationJob([id:spectrum.id])
            return
        }


        log.debug("persisting spectrum: ${spectrum.id}")
        Date date = spectrum.getDateCreated()
        long begin = System.currentTimeMillis()
        Calendar cal = Calendar.getInstance()
        cal.setTime(date)


        //where are we going to store our dataget).inchiKey}/${spectrum.getSplash().splash}/")

        log.debug("storing at directory: ${directory}")
        if (!directory.exists()) {
            log.debug("\t-> creating directory")
            if (!directory.mkdirs()) {
                throw new FileNotFoundException("sorry, was not able to create the required directory: ${directory.absolutePath}")
            }
        }

        //generating the actual json file
        File outputFile = new File(directory, "${spectrum.id}.json")

        if (outputFile.exists()) {
            log.debug("\t-> overwriting existing file!")
        }
        persist(outputFile, spectrum)

        statisticsService.acquire(System.currentTimeMillis() - begin, "${spectrum.id}", "duration of exporting spectra to repository", "repository-export")

    }

    /**
     * persisting the actual file somehow
     * @param outputFile
     * @param spectrum
     */
    private void persist(File outputFile, Spectrum spectrum) {
        BufferedWriter stream = new BufferedWriter(new FileWriter(outputFile))

        String content = (spectrum as JSON).toString(true)

        log.trace("serialized as: \n\n${content}")
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
            FireJobs.fireSpectraRepositoryExportJob([id: id])

        }
    }

    def exportAllToRepository(){
        log.info("persisting all  spectra of the last to the repository")
        Spectrum.executeQuery("select s.id from Spectrum s ").each { Long id ->

            //schedule a new job to parallize things a bit
            FireJobs.fireSpectraRepositoryExportJob([id: id])

        }
    }

}
