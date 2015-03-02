package moa.server

import grails.transaction.Transactional
import grails.validation.ValidationException
import moa.News
import moa.Spectrum

@Transactional
class NewsService {

    /**
     * deletes all outdated news
     * @return
     */
    def removeOutDatedNews() {


        def news = News.findAllByExpiresLessThan(System.currentTimeMillis())

        News.deleteAll(news)

        log.debug("news are deleted!")

    }

    /**
     * submitts a news itume for a new spectra
     * @param spectrum
     * @return
     */
    def spectraCreatedNews(Spectrum spectrum) {

        String message = "a spectrum was just submitted by ${spectrum.submitter.firstName} ${spectrum.submitter.lastName} for "

        if(spectrum.chemicalCompound.names != null && spectrum.chemicalCompound.names.size() > 0){
            message += spectrum.chemicalCompound.names[0].name
        }
        else{
            message += spectrum.chemicalCompound.inchiKey
        }

        createNews(
                "spectrum created: ${spectrum.id}",
                message,
                "/spectra/display/${spectrum.id}",
                60,
                News.UPLOAD,
                "spectra"
        )
    }

    def createNews(String title, String message, String url = "none", long lifetime = Long.MAX_VALUE, String type = News.ANNOUNCEMENT, String iconClass = "none") {

        log.info("creating new news item: ${title}")
        News news = new News()
        news.title = title

        if (lifetime == Long.MAX_VALUE) {
            news.expires = lifetime
        } else {
            news.expires = System.currentTimeMillis() + (lifetime * 1000)
        }
        news.description = message
        news.type = type
        news.iconClass = iconClass
        news.url = url

        if (!news.validate()) {
            throw new ValidationException("sorry news item is not valid", news.errors)
        }
        news.save()

    }
}
