import grails.converters.JSON
import grails.util.Environment
import moa.Compound
import moa.MetaData
import moa.MetaDataCategory
import moa.MetaDataValue
import moa.Name
import moa.Spectrum
import moa.Submitter
import moa.Tag
import moa.MetaDataValue
import util.DomainClassMarshaller

import org.hibernate.FlushMode
import util.marshallers.TagMarshaller

class BootStrap {

    def sessionFactory

    def init = { servletContext ->

        def session = sessionFactory.currentSession

        session.setFlushMode(FlushMode.COMMIT)

        if (Environment.isDevelopmentMode()) {
            //just some test data
            /*
            new Submitter(firstName: "Gert", lastName: "Wohlgemuth", emailAddress: "wohlgemuth@ucdavis.edu", password: "dasdsa").save()
            new Submitter(firstName: "Diego", lastName: "Pedrosa", emailAddress: "linuxmant@gmail.com", password: "dsadasd").save()
            new Submitter(firstName: "Oliver", lastName: "Fiehn", emailAddress: "ofiehn@ucdavis.edu", password: "sdsadsad").save()


            Tag.findOrCreateWhere(text: "dirty").save()
            Tag.findOrCreateWhere(text: "clean").save()
            Tag.findOrCreateWhere(text: "mixed").save()
            Tag.findOrCreateWhere(text: "standard").save()
            Tag.findOrCreateWhere(text: "injected").save()
            Tag.findOrCreateWhere(text: "experimental").save()

            MetaDataCategory.findOrCreateByName("computed").save()
            MetaDataCategory.findOrCreateByNameAndVisible("annotation",false).save()
              */

        }

        JSON.registerObjectMarshaller(Tag,
                DomainClassMarshaller.createExcludeMarshaller(Tag, ["class","id"])
        )

        JSON.registerObjectMarshaller(Compound,
                DomainClassMarshaller.createExcludeMarshaller(Compound, ["class", "spectra"])
        )

        JSON.registerObjectMarshaller(Submitter,
                DomainClassMarshaller.createExcludeMarshaller(Submitter, ["class", "spectra","password"])
        )

        JSON.registerObjectMarshaller(Spectrum,
                DomainClassMarshaller.createExcludeMarshaller(Spectrum, ["class"])
        )
        JSON.registerObjectMarshaller(Name,
                DomainClassMarshaller.createExcludeMarshaller(Name, ["class","id","compound"])
        )
        JSON.registerObjectMarshaller(MetaData,
                DomainClassMarshaller.createExcludeMarshaller(MetaData, ["class","value"])
        )
        JSON.registerObjectMarshaller(MetaDataValue,
                DomainClassMarshaller.createExcludeMarshaller(MetaDataValue, ["class","id","owner","metaData"])
        )
        JSON.registerObjectMarshaller(MetaDataCategory,
                DomainClassMarshaller.createExcludeMarshaller(MetaDataCategory, ["class","metaDatas"])
        )



    }

    def destroy = {
    }
}
