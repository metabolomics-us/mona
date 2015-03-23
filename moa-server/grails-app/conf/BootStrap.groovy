import grails.converters.JSON
import groovy.sql.Sql
import moa.*
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

        //harddcoded submiters for now
        Submitter.findOrCreateWhere(firstName: "Gert", lastName: "Wohlgemuth", emailAddress: "wohlgemuth@ucdavis.edu", password: "password", institution: "University of California, Davis").save()
        Submitter.findOrCreateWhere(firstName: "Sajjan", lastName: "Mehta", emailAddress: "ssmehta@ucdavis.edu", password: "password", institution: "University of California, Davis").save()
        Submitter.findOrCreateWhere(firstName: "Diego", lastName: "Pedrosa", emailAddress: "linuxmant@gmail.com", password: "password", institution: "University of California, Davis").save()
        Submitter.findOrCreateWhere(firstName: "Megan", lastName: "Showalter", emailAddress: "mshowalter@ucdavis.edu", password: "password", institution: "University of California, Davis").save()

        //riken data
        Submitter.findOrCreateWhere(firstName: "Hiroshi", lastName: "Tusgawa", emailAddress: "hiroshi.tsugawa@riken.jp", password: "password", institution: "Riken, Japan").save()
        Submitter.findOrCreateWhere(firstName: "Akie", lastName: "Mejia", emailAddress: "rfmejia@gmail.com", password: "password", institution: "Riken, Japan").save()


        // Submitter roles

        def addRole = { String  email,boolean admin ->
            Submitter s = Submitter.findOrCreateByEmailAddress("wohlgemuth@ucdavis.edu")
            Role.findOrCreateByAuthority('ROLE_ADMIN').save()
            Role.findOrCreateByAuthority('ROLE_CURATOR').save()
            Role.findOrCreateByAuthority('ROLE_USER').save()

            Role a = Role.findOrCreateByAuthority('ROLE_ADMIN')
            Role u = Role.findOrCreateByAuthority('ROLE_USER')

            if(admin) {
                if (SubmitterRole.findBySubmitterAndRole(s, a) == null) {
                    SubmitterRole.create(Submitter.findOrCreateByEmailAddress(email), Role.findOrCreateByAuthority('ROLE_ADMIN'))
                }
            }

            if (SubmitterRole.findBySubmitterAndRole(s, u) == null) {
                SubmitterRole.create(Submitter.findOrCreateByEmailAddress(email), Role.findOrCreateByAuthority('ROLE_USER'))
            }

        }

        addRole("wohlgemuth@ucdavis.edu",true)
        addRole("ssmehta@ucdavis.edu",true)
        addRole("linuxmant@gmail.com",true)
        addRole("mshowalter@ucdavis.edu",false)


        addRole("hiroshi.tsugawa@riken.jp",false)
        addRole("rfmejia@gmail.com",false)



        JSON.registerObjectMarshaller(Tag,
                DomainClassMarshaller.createExcludeMarshaller(Tag, ["class", "id", "tagCachingService", "dateCreated", "lastUpdated"])
        )

        JSON.registerObjectMarshaller(Compound,
                DomainClassMarshaller.createExcludeMarshaller(Compound, ["class", "spectra", "dateCreated"])
        )

        JSON.registerObjectMarshaller(Submitter,
                DomainClassMarshaller.createExcludeMarshaller(Submitter, ["class", "spectra", "password", "dateCreated", "lastUpdated"])
        )

        JSON.registerObjectMarshaller(Spectrum,
                DomainClassMarshaller.createExcludeMarshaller(Spectrum, ["class", "dateCreated", "ions"])
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
