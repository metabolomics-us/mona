import moa.Submitter

class BootStrap {

    def init = { servletContext ->

        //just some test data
        new Submitter(firstName: "Gert", lastName: "Wohlgemuth",emailAddress: "wohlgemuth@ucdavis.edu",password: "dasdsa").save()
        new Submitter(firstName: "Diego", lastName: "Pedrosa",emailAddress: "linuxmant@gmail.com",password: "dsadasd").save()
        new Submitter(firstName: "Oliver", lastName: "Fiehn",emailAddress: "ofiehn@ucdavis.edu",password: "sdsadsad").save()

    }
    def destroy = {
    }
}
