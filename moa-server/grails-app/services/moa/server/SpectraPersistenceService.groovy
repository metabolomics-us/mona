package moa.server
import moa.*
import moa.server.caluclation.CompoundPropertyService
import moa.server.metadata.MetaDataPersistenceService

class SpectraPersistenceService {

    MetaDataPersistenceService metaDataPersistenceService

    CompoundPropertyService compoundPropertyService

    /**
     * creates a new spectrum and saves it in the database
     * @param params
     * @return
     */
    public  Spectrum create(Map json) {

        Spectrum spectrum = new Spectrum(json)

        log.debug("inserting new spectra: ${spectrum.spectrum}")

        //we build the metadata rather our self
        spectrum.metaData = [];

        //we build the tags our self
        spectrum.tags = [];

        //we only care about refreshing the submitter by it's email address since it's unique
        spectrum.submitter = Submitter.findByEmailAddress(spectrum.submitter.emailAddress)

        //we need to ensure we don't double generate compound
        spectrum.chemicalCompound = buildCompound(spectrum.chemicalCompound).save()
        spectrum.biologicalCompound = buildCompound(spectrum.biologicalCompound).save()

        spectrum.save()
        spectrum.lock()

        if (json.tags) {
            def tags = json.tags

            //adding our tags
            tags.each {

                def tag = Tag.findOrSaveByText(it.text)
                tag.refresh()
                spectrum.addToTags(tag)
            }
        }

        metaDataPersistenceService.generateMetaDataFromJson(spectrum, json.metaData)

        spectrum.save(flush: true)

        //spectrum is now ready to work on
        return spectrum;

    }

    /**
     * generates a correctly implemted metadata set
     * @param object - object to modify
     * @parm json - json definition of the metadata
     * @return
     */

/**
 * builds our internal compound object
 * @param compound
 * @return
 */
    private Compound buildCompound(Compound compound) {

        def names = compound.names

        log.debug("trying to generate compound: ${compound.inchiKey}")

        //first get the compound we want
        def myCompound = Compound.findByInchiKey(compound.inchiKey.trim())

        if (!myCompound) {
            Compound.withTransaction {
                myCompound = new Compound(inchiKey: compound.inchiKey.trim())

                myCompound.save()
            }
        }

        myCompound.lock()

        log.debug("==> done: ${myCompound}")

        //merge new names
        names.each { name ->
            Name n = Name.findByNameAndCompound(name.name, myCompound)
            if (n != null) {
                myCompound.addToNames(new Name(name: name))
            }
        }

        myCompound.molFile = compound.molFile

        myCompound.save(flush:true)

        compoundPropertyService.calculateMetaData(myCompound)

        return myCompound;


    }

}
