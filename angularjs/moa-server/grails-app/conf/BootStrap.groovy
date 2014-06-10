import moa.Submitter
import moa.Tag

class BootStrap {

    def init = { servletContext ->

        //just some test data
        new Submitter(firstName: "Gert", lastName: "Wohlgemuth",emailAddress: "wohlgemuth@ucdavis.edu",password: "dasdsa").save()
        new Submitter(firstName: "Diego", lastName: "Pedrosa",emailAddress: "linuxmant@gmail.com",password: "dsadasd").save()
        new Submitter(firstName: "Oliver", lastName: "Fiehn",emailAddress: "ofiehn@ucdavis.edu",password: "sdsadsad").save()


        new Tag(text: "dirty").save()
        new Tag(text: "clean").save()
        new Tag(text: "mixed").save()
        new Tag(text: "standard").save()
        new Tag(text: "injected").save()
        new Tag(text: "experimental").save()

    }
    def destroy = {
    }
}
