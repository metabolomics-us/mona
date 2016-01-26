import grails.converters.JSON
import moa.*
import moa.query.Query
import moa.scoring.Impact
import moa.scoring.Score
import moa.server.NewsService
import moa.auth.Role
import moa.auth.SubmitterRole
import moa.splash.Splash
import util.DomainClassMarshaller
import util.query.StaticQueries

class BootStrap {

    NewsService newsService


    def sessionFactory

    def init = { servletContext ->
        def session = sessionFactory.currentSession
        //session.setFlushMode(FlushMode.COMMIT)

        log.info("Setting up users and marshallers...")

        // Submitter roles
        Role adminRole = Role.findOrCreateByAuthority('ROLE_ADMIN').save()
        Role curatorRole = Role.findOrCreateByAuthority('ROLE_CURATOR').save()
        Role userRole = Role.findOrCreateByAuthority('ROLE_USER').save()

        def addUser = { String firstName, String lastName, String emailAddress, String password, String institution, boolean isAdmin ->
            Submitter s = Submitter.findOrCreateWhere(firstName: firstName, lastName: lastName, emailAddress: emailAddress, password: password, institution: institution).save()
            SubmitterRole.findOrCreateWhere(submitter: s, role: userRole).save()

            if(isAdmin) {
                SubmitterRole.findOrCreateWhere(submitter: s, role: curatorRole).save()
                SubmitterRole.findOrCreateWhere(submitter: s, role: adminRole).save()
            }
        }

        // Fiehnlab Members
        addUser("Gert", "Wohlgemuth", "wohlgemuth@ucdavis.edu", "password", "University of California, Davis", true)
        addUser("Sajjan", "Mehta", "ssmehta@ucdavis.edu", "password", "University of California, Davis", true)
        addUser("Diego", "Pedrosa", "dpedrosa@ucdavis.edu", "password", "University of California, Davis", true)


        // Register domain object marshallers
        JSON.registerObjectMarshaller(Tag,
                DomainClassMarshaller.createExcludeMarshaller(Tag, ["links", "class", "id", "tagCachingService", "dateCreated", "lastUpdated", "owner"])
        )
        JSON.registerObjectMarshaller(Compound,
                DomainClassMarshaller.createExcludeMarshaller(Compound, ["links","class", "spectra", "dateCreated"])
        )
        JSON.registerObjectMarshaller(Submitter,
                DomainClassMarshaller.createExcludeMarshaller(Submitter, ["class", "spectra", "password", "dateCreated", "lastUpdated"])
        )
        JSON.registerObjectMarshaller(Role,
                DomainClassMarshaller.createExcludeMarshaller(Role, ["class", "id"])
        )
        JSON.registerObjectMarshaller(Spectrum,
                DomainClassMarshaller.createExcludeMarshaller(Spectrum, ["links", "class", "dateCreated", "ions","compoundLinks"])
        )
        JSON.registerObjectMarshaller(Name,
                DomainClassMarshaller.createExcludeMarshaller(Name, ["class", "id", "compound", "dateCreated"])
        )
        JSON.registerObjectMarshaller(MetaData,
                DomainClassMarshaller.createExcludeMarshaller(MetaData, ["class", "value", "dateCreated"])
        )
        JSON.registerObjectMarshaller(MetaDataValue,
                DomainClassMarshaller.createExcludeMarshaller(MetaDataValue, ["class", "owner", "metaData"])
        )
        JSON.registerObjectMarshaller(MetaDataCategory,
                DomainClassMarshaller.createExcludeMarshaller(MetaDataCategory, ["class", "metaData", "dateCreated"])
        )
        JSON.registerObjectMarshaller(Comment,
                DomainClassMarshaller.createExcludeMarshaller(Comment, ["class", "dateCreated"])
        )
        JSON.registerObjectMarshaller(News,
                DomainClassMarshaller.createExcludeMarshaller(News, ["class"])
        )
        JSON.registerObjectMarshaller(Score,
                DomainClassMarshaller.createExcludeMarshaller(Score, ["class", "id"])
        )
        JSON.registerObjectMarshaller(Impact,
                DomainClassMarshaller.createExcludeMarshaller(Impact, ["class", "score", "id", "scoringClass"])
        )
        JSON.registerObjectMarshaller(Query,
                DomainClassMarshaller.createExcludeMarshaller(Query, ["class"])
        )
        JSON.registerObjectMarshaller(SpectrumQueryDownload,
                DomainClassMarshaller.createExcludeMarshaller(SpectrumQueryDownload, ["class", "queryFile", "exportFile", "emailAddress"])
        )
        JSON.registerObjectMarshaller(Splash,
                DomainClassMarshaller.createExcludeMarshaller(Splash, ["class", "spectrum"])
        )
//        JSON.registerObjectMarshaller(Ion,
//                DomainClassMarshaller.createExcludeMarshaller(Ion, ["class","spectrum","id","dateCreated","lastUpdated"])
//        )


        // Generate static queries
        StaticQueries.register()
    }

    def destroy = {
    }
}
