import grails.converters.JSON
import groovy.sql.Sql
import moa.*
import moa.server.NewsService
import org.hibernate.FlushMode
import util.DomainClassMarshaller

class BootStrap {

    NewsService newsService


    def sessionFactory

    def init = { servletContext ->

        def session = sessionFactory.currentSession

        session.setFlushMode(FlushMode.COMMIT)

        log.warn("in development mode, setting up users...")
        //just some test data
        Submitter.findOrCreateWhere(firstName: "Gert", lastName: "Wohlgemuth", emailAddress: "wohlgemuth@ucdavis.edu", password: "dasdsa").save()

        JSON.registerObjectMarshaller(Tag,
                DomainClassMarshaller.createExcludeMarshaller(Tag, ["class", "id", "tagCachingService","dateCreated","lastUpdated"])
        )

        JSON.registerObjectMarshaller(Compound,
                DomainClassMarshaller.createExcludeMarshaller(Compound, ["class", "spectra","dateCreated"])
        )

        JSON.registerObjectMarshaller(Submitter,
                DomainClassMarshaller.createExcludeMarshaller(Submitter, ["class", "spectra", "password","dateCreated","lastUpdated"])
        )

        JSON.registerObjectMarshaller(Spectrum,
                DomainClassMarshaller.createExcludeMarshaller(Spectrum, ["class","dateCreated","ions"])
        )
        JSON.registerObjectMarshaller(Name,
                DomainClassMarshaller.createExcludeMarshaller(Name, ["class", "id", "compound","dateCreated"])
        )
        JSON.registerObjectMarshaller(MetaData,
                DomainClassMarshaller.createExcludeMarshaller(MetaData, ["class", "value","dateCreated"])
        )
        JSON.registerObjectMarshaller(MetaDataValue,
                DomainClassMarshaller.createExcludeMarshaller(MetaDataValue, ["class", "id", "owner", "metaData"])
        )
        JSON.registerObjectMarshaller(MetaDataCategory,
                DomainClassMarshaller.createExcludeMarshaller(MetaDataCategory, ["class", "metaDatas","dateCreated"])
        )
        JSON.registerObjectMarshaller(Comment,
                DomainClassMarshaller.createExcludeMarshaller(Comment, ["class","dateCreated"])
        )
        JSON.registerObjectMarshaller(News,
                DomainClassMarshaller.createExcludeMarshaller(News, ["class"])
        )

        /*
        JSON.registerObjectMarshaller(Ion,
                DomainClassMarshaller.createExcludeMarshaller(Ion, ["class","spectrum","id","dateCreated","lastUpdated"])
        )

*/




        if(News.getCount() == 0){
            newsService.createNews("test","a simple test")
            newsService.createNews("test2","a simple test 2")
            newsService.createNews("test3","a simple test 3")
            newsService.createNews("test4","a simple test 4")

        }

    }

    def destroy = {
    }
}
