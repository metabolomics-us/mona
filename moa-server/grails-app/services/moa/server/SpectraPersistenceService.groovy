package moa.server

import moa.*
import moa.server.caluclation.CompoundPropertyService
import moa.server.metadata.MetaDataPersistenceService
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject
import org.grails.datastore.mapping.validation.ValidationException

class SpectraPersistenceService {

    MetaDataPersistenceService metaDataPersistenceService

    CompoundPropertyService compoundPropertyService

    /**
     * creates a new spectrum and saves it in the database
     * @param params
     * @return
     */
    public Spectrum create(JSONObject json) {
        //handle outdated format
        if (json.comments instanceof String) {
            log.warn("using out dated Mona format, comment's should be in form of an array -> skipping attribute!")
            String value = json.get("comments")
            json.remove("comments")

            JSONArray array = new JSONArray();
            JSONObject comment = new JSONObject();
            comment.put("comment", value)

            array.add(comment)
            json.put("comments", array)
        }

        if (json.id) {
            log.warn("dropping existing id...")
            json.remove("id")
        }

        Spectrum spectrum = new Spectrum(json)

        log.debug("inserting new spectra")

        //we build the metadata rather our self
        spectrum.metaData = [];

        //we build the tags our self
        spectrum.tags = [];

        //we only care about refreshing the submitter by it's email address since it's unique
        spectrum.submitter = Submitter.findByEmailAddress(spectrum.submitter.emailAddress)

        //we need to ensure we don't double generate compound
        spectrum.chemicalCompound = buildCompound(spectrum.chemicalCompound).save()
        spectrum.biologicalCompound = buildCompound(spectrum.biologicalCompound).save()

        if (!spectrum.validate()) {
            log.error(spectrum.errors)
            throw new ValidationException("sorry was not able to persist spectra", spectrum.errors)
        }

        spectrum.save(flush: true)

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
    public Compound buildCompound(Compound compound) {

        def names = compound.names

        log.debug("trying to generate compound: ${compound.inchiKey}")

        //first get the compound we want
        def myCompound = Compound.findOrSaveByInchiKey(compound.inchiKey.trim())

        /*
        if (!myCompound) {
            Compound.withTransaction {
                myCompound = new Compound(inchiKey: compound.inchiKey.trim())

                myCompound.save(flush: true)
            }
        }
        */

        myCompound.lock()

        log.debug("==> done: ${myCompound}")

        if (names) {
            //merge new names
            names.each { name ->
                Name n = Name.findOrSaveByNameAndCompound(name.name, myCompound)
                if (n != null) {
                    myCompound.addToNames(n)
                }
            }
        }

        myCompound.molFile = compound.molFile
        myCompound.inchi = compound.inchi

        myCompound.save(flush: true)

        compoundPropertyService.calculateMetaData(myCompound)

        return myCompound;

    }

}
