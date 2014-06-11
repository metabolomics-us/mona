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

		/*
		{ "biologicalCompound": { "inchi": "QNAYBMKLOCPYGJ-UWTATZPHSA-N", "name": "(R)-2-aminopropanoic acid" },
		"chemicalCompound": { "inchi": "QNAYBMKLOCPYGJ-UWTATZPHSA-N", "name": "DAL" },
		"tags": [ { "text": "dirty" }, { "text": "mixed" }, { "text": "injected" }, { "text": "standard" } ],
		"metadata": [],
		"rawData": "123:13312 124:1233" }
		 */

		Compound bio = new Compound(inchiKey: "QNAYBMKLOCPYGJ-UWTATZPHSA-N", names: ["(R)-2-aminopropanoic acid"]).save()
		Compound chem = new Compound(inchiKey: "QNAYBMKLOCPYGJ-UWTATZPHSA-N", names: ["DAL"]).save()
		List<Tag> tags = new ArrayList<Tag>()
		tags.add(new Tag(value:"dirty").save())
		tags.add(new Tag(value:"mixed").save())
		tags.add(new Tag(value:"injected").save())
		tags.add(new Tag(value:"standard").save())

		new Spectrum(compoundBio: bio, compoundChem: chem, tags:tags, spectrum: "123:13312 124:1233").save(flush: true)
	}

	def destroy = {
	}
}
