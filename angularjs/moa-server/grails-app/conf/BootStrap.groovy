import moa.Compound
import moa.Spectrum
import moa.Submitter
import moa.Tag

class BootStrap {

    def init = { servletContext ->

        //just some test data
        new Submitter(firstName: "Gert", lastName: "Wohlgemuth", emailAddress: "wohlgemuth@ucdavis.edu", password: "dasdsa").save()
        new Submitter(firstName: "Diego", lastName: "Pedrosa", emailAddress: "linuxmant@gmail.com", password: "dsadasd").save()
        new Submitter(firstName: "Oliver", lastName: "Fiehn", emailAddress: "ofiehn@ucdavis.edu", password: "sdsadsad").save()


        Tag.findOrCreateWhere(text: "dirty")
        Tag.findOrCreateWhere(text: "clean")
        Tag.findOrCreateWhere(text: "mixed")
        Tag.findOrCreateWhere(text: "standard")
        Tag.findOrCreateWhere(text: "injected")
        Tag.findOrCreateWhere(text: "experimental")

    }

    def destroy = {
    }
}
