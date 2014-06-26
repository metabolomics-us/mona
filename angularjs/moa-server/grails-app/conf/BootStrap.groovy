import grails.converters.JSON
import grails.util.Environment
import moa.Compound
import moa.Name
import moa.Spectrum
import moa.Submitter
import moa.Tag
import util.DomainClassMarshaller

class BootStrap {

    def init = { servletContext ->

        if (Environment.isDevelopmentMode()) {
            //just some test data
            new Submitter(firstName: "Gert", lastName: "Wohlgemuth", emailAddress: "wohlgemuth@ucdavis.edu", password: "dasdsa").save()
            new Submitter(firstName: "Diego", lastName: "Pedrosa", emailAddress: "linuxmant@gmail.com", password: "dsadasd").save()
            new Submitter(firstName: "Oliver", lastName: "Fiehn", emailAddress: "ofiehn@ucdavis.edu", password: "sdsadsad").save()


            Tag.findOrCreateWhere(text: "dirty").save()
            Tag.findOrCreateWhere(text: "clean").save()
            Tag.findOrCreateWhere(text: "mixed").save()
            Tag.findOrCreateWhere(text: "standard").save()
            Tag.findOrCreateWhere(text: "injected").save()
            Tag.findOrCreateWhere(text: "experimental").save()

        }
        JSON.registerObjectMarshaller(Tag,
                DomainClassMarshaller.createExcludeMarshaller(Tag, ["class"])
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
                DomainClassMarshaller.createExcludeMarshaller(Name, ["class"])
        )

    }

    def destroy = {
    }
}
