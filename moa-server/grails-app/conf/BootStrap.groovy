import grails.converters.JSON
import moa.*
import org.hibernate.FlushMode
import util.DomainClassMarshaller

class BootStrap {

    def sessionFactory

    def init = { servletContext ->

        def session = sessionFactory.currentSession

        session.setFlushMode(FlushMode.COMMIT)

        log.warn("in development mode, setting up users...")
        //just some test data
        Submitter.findOrCreateWhere(firstName: "Gert", lastName: "Wohlgemuth", emailAddress: "wohlgemuth@ucdavis.edu", password: "dasdsa").save()

        JSON.registerObjectMarshaller(Tag,
                DomainClassMarshaller.createExcludeMarshaller(Tag, ["class", "id", "tagCachingService"])
        )

        JSON.registerObjectMarshaller(Compound,
                DomainClassMarshaller.createExcludeMarshaller(Compound, ["class", "spectra"])
        )

        JSON.registerObjectMarshaller(Submitter,
                DomainClassMarshaller.createExcludeMarshaller(Submitter, ["class", "spectra", "password"])
        )

        JSON.registerObjectMarshaller(Spectrum,
                DomainClassMarshaller.createExcludeMarshaller(Spectrum, ["class"])
        )
        JSON.registerObjectMarshaller(Name,
                DomainClassMarshaller.createExcludeMarshaller(Name, ["class", "id", "compound"])
        )
        JSON.registerObjectMarshaller(MetaData,
                DomainClassMarshaller.createExcludeMarshaller(MetaData, ["class", "value"])
        )
        JSON.registerObjectMarshaller(MetaDataValue,
                DomainClassMarshaller.createExcludeMarshaller(MetaDataValue, ["class", "id", "owner", "metaData"])
        )
        JSON.registerObjectMarshaller(MetaDataCategory,
                DomainClassMarshaller.createExcludeMarshaller(MetaDataCategory, ["class", "metaDatas"])
        )
        JSON.registerObjectMarshaller(Comment,
                DomainClassMarshaller.createExcludeMarshaller(Comment, ["class"])
        )
        JSON.registerObjectMarshaller(Ion,
                DomainClassMarshaller.createExcludeMarshaller(Ion, ["class","spectrum","id"])
        )



    }

    def destroy = {
    }
}
