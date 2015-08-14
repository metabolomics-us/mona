package moa.server.scoring

import curation.CurationObject
import curation.scoring.Scoreable
import curation.scoring.ScoringWorkflow
import grails.transaction.Transactional
import moa.News
import moa.Spectrum
import moa.scoring.Impact
import moa.scoring.Score
import moa.server.NewsService
import moa.server.metadata.MetaDataPersistenceService
import moa.server.statistics.StatisticsService

@Transactional
class ScoringService {

    NewsService newsService

    StatisticsService statisticsService

    ScoringWorkflow spectraScoringWorkflow

    MetaDataPersistenceService metaDataPersistenceService

    /**
     * adjusts the score of the spectrum
     * @param spectrum
     * @param impact
     * @return
     */
    def adjustScore(Scoreable scoreable, Impact impact) {

        if(impact.impactValue > 0 || impact.impactValue < 0) {
            if (scoreable.score == null) {
                Score score = new Score()
                scoreable.setScore(score)
                scoreable.save()
            }

            Score score = scoreable.score

            impact.score = score
            score.addToImpacts(impact)

            score.save()
            log.info("adjusted score to ${score.score} for ${scoreable} using  ${impact}")
        }
        else{
            log.debug("impact's with value of 0, will always be ignored since they are not doing anything!")
        }
    }

    /**
     * drops the score from the given spectrum
     * @param spectrum
     * @return
     */
    def dropScore(Scoreable scoreable) {
        log.debug("remove existing score!")
        Score score = scoreable.score
        if (score) {
            if (score.impacts != null) {

                def toDelete = []

                score.impacts.each {
                    toDelete.add(it)
                }

                toDelete.each { Impact impact1 ->
                    impact1.score = null
                    score.removeFromImpacts(impact1)
                    impact1.delete()
                }

                score.impacts.clear()
                score.save()
            }

            scoreable.score = null
            scoreable.save(flush:true)
            score.delete(flush: true)
        }
    }

    /**
     * scores the provided spectra for us
     * @param id
     */
    def score(long id) {
        score(Spectrum.get(id))
    }

    /**
     * scores the provided spectra for us
     * @param spectrum
     */
    def score(Spectrum spectrum) {
        long begin = System.currentTimeMillis()

        if (spectrum) {

            dropScore(spectrum)
            boolean result = spectraScoringWorkflow.runWorkflow(new CurationObject(spectrum))

            long end = System.currentTimeMillis()

            long needed = (end - begin)

            spectrum = Spectrum.get(spectrum.id)

            //add some metadata that we did some cool stuff
            metaDataPersistenceService.generateMetaDataObject(spectrum, [name: "scoring date", value: new Date().format("dd-MMM-yyyy"), category: "computed", computed: true])
            metaDataPersistenceService.generateMetaDataObject(spectrum, [name: "scoring time", value: needed, unit: "ms", category: "computed", computed: true])

            statisticsService.acquire(needed, "${spectrum.id}", "spectra scoring time", "scoring")

            spectrum.save()

            //some notification for other people
            String message = "a spectrum was just scored for "

            if (spectrum.chemicalCompound.names != null && spectrum.chemicalCompound.names.size() > 0) {
                message += spectrum.chemicalCompound.names[0].name
            } else {
                message += spectrum.chemicalCompound.inchiKey
            }

            newsService.createNews(
                    "spectrum scored: ${spectrum.id}",
                    message,
                    "/spectra/display/${spectrum.id}",
                    60,
                    News.NOTIFICATION,
                    "spectra"
            )

            if (result) {
                return spectrum.score.getScore()
            } else {
                return false
            }
        } else {
            return false
        }

    }
}
