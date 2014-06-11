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


        new Tag(text: "dirty").save()
        new Tag(text: "clean").save()
        new Tag(text: "mixed").save()
        new Tag(text: "standard").save()
        new Tag(text: "injected").save()
        new Tag(text: "experimental").save()

        /*
        { "biologicalCompound": { "inchi": "QNAYBMKLOCPYGJ-UWTATZPHSA-N", "name": "(R)-2-aminopropanoic acid" },
        "chemicalCompound": { "inchi": "QNAYBMKLOCPYGJ-UWTATZPHSA-N", "name": "DAL" },
        "tags": [ { "text": "dirty" }, { "text": "mixed" }, { "text": "injected" }, { "text": "standard" } ],
        "metadata": [],
        "rawData": "123:13312 124:1233" }
         */

        Compound bio = new Compound(inchiKey: "QNAYBMKLOCPYGJ-UWTATZPHSA-N", names: ["(R)-2-aminopropanoic acid"]).save()
        Compound chem = new Compound(inchiKey: "QNAYBMKLOCPYGJ-UWTATZPHSA-N", names: ["DAL"]).save()

        new Spectrum(compoundBio: bio, compoundChem: chem, tags: Tag.list(), spectrum: "123:13312 124:1233").save(flush: true)
    }

    def destroy = {
    }
}
