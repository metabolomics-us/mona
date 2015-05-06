import grails.converters.JSON
import groovy.sql.Sql
import moa.*
import moa.scoring.Impact
import moa.scoring.Score
import moa.server.NewsService
import moa.auth.Role
import moa.auth.SubmitterRole
import org.hibernate.FlushMode
import util.DomainClassMarshaller

class BootStrap {

    NewsService newsService


    def sessionFactory

    def init = { servletContext ->

        def session = sessionFactory.currentSession

        //session.setFlushMode(FlushMode.COMMIT)

        log.warn("in development mode, setting up users...")

        // Submitter roles
        Role adminRole = Role.findOrCreateByAuthority('ROLE_ADMIN').save()
        Role curatorRole = Role.findOrCreateByAuthority('ROLE_CURATOR').save()
        Role userRole = Role.findOrCreateByAuthority('ROLE_USER').save()

        def addUser = { String firstName, String lastName, String emailAddress, String password, String institution, boolean isAdmin ->
            Submitter s = Submitter.findOrCreateWhere(firstName: firstName, lastName: lastName, emailAddress: emailAddress, password: password, institution: institution).save()
            SubmitterRole.create(s, userRole)

            if(isAdmin) {
                SubmitterRole.create(s, curatorRole)
                SubmitterRole.create(s, adminRole)
            }
        }

        // Fiehnlab
        addUser("Gert", "Wohlgemuth", "wohlgemuth@ucdavis.edu", "password", "University of California, Davis", true)
        addUser("Sajjan", "Mehta", "ssmehta@ucdavis.edu", "password", "University of California, Davis", true)
        addUser("Diego", "Pedrosa", "linuxmant@gmail.com", "password", "University of California, Davis", true)
        addUser("Megan", "Showalter", "mshowalter@ucdavis.edu", "password", "University of California, Davis", false)
        addUser("Yan", "Ma", "yanma@ucdavis.edu", "password", "University of California, Davis", false)

        // RIKEN
        addUser("Hiroshi", "Tusgawa", "hiroshi.tsugawa@riken.jp", "password", "Riken, Japan", false)
        addUser("Akie", "Mejia", "rfmejia@gmail.com", "password", "Riken, Japan", false)



        JSON.registerObjectMarshaller(Tag,
                DomainClassMarshaller.createExcludeMarshaller(Tag, ["links","class", "id", "tagCachingService", "dateCreated", "lastUpdated","owner"])
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
                DomainClassMarshaller.createExcludeMarshaller(Spectrum, ["links","class", "dateCreated", "ions"])
        )
        JSON.registerObjectMarshaller(Name,
                DomainClassMarshaller.createExcludeMarshaller(Name, ["class", "id", "compound", "dateCreated"])
        )
        JSON.registerObjectMarshaller(MetaData,
                DomainClassMarshaller.createExcludeMarshaller(MetaData, ["class", "value", "dateCreated"])
        )
        JSON.registerObjectMarshaller(MetaDataValue,
                DomainClassMarshaller.createExcludeMarshaller(MetaDataValue, ["class", "id", "owner", "metaData"])
        )
        JSON.registerObjectMarshaller(MetaDataCategory,
                DomainClassMarshaller.createExcludeMarshaller(MetaDataCategory, ["class", "metaDatas", "dateCreated"])
        )
        JSON.registerObjectMarshaller(Comment,
                DomainClassMarshaller.createExcludeMarshaller(Comment, ["class", "dateCreated"])
        )
        JSON.registerObjectMarshaller(News,
                DomainClassMarshaller.createExcludeMarshaller(News, ["class"])
        )
        JSON.registerObjectMarshaller(Score,
                DomainClassMarshaller.createExcludeMarshaller(Score, ["class","id"])
        )
        JSON.registerObjectMarshaller(Impact,
                DomainClassMarshaller.createExcludeMarshaller(Impact, ["class","score","id","scoringClass"])
        )



        /*
        JSON.registerObjectMarshaller(Ion,
                DomainClassMarshaller.createExcludeMarshaller(Ion, ["class","spectrum","id","dateCreated","lastUpdated"])
        )
        */

        //newsService.createNews("massbank upload","the upload of massbank was complete!","none")
    }

    def destroy = {
    }
}
