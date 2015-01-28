package moa.server

import grails.plugin.cache.CacheEvict
import moa.Ion
import moa.Spectrum
import moa.server.metadata.MetaDataPersistenceService
import moa.server.tag.TagService
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject
import org.grails.datastore.mapping.validation.ValidationException

//@Transactional
class SpectraPersistenceService {

    MetaDataPersistenceService metaDataPersistenceService

    TagService tagService

    SubmitterService submitterService

    CompoundService compoundService

    /**
     * creates a new spectrum and saves it in the database
     * @param params
     * @return
     */
    @CacheEvict(value = 'spectrum', allEntries = true)
    public Spectrum create(Map json) {

        //handle outdated format
        if (json.comments instanceof String) {
            log.debug("using out dated Mona format, comment's should be in form of an array -> skipping attribute!")
            String value = json.get("comments")
            json.remove("comments")

            JSONArray array = new JSONArray();
            JSONObject comment = new JSONObject();
            comment.put("comment", value)

            array.add(comment)
            json.put("comments", array)
        }

        if (json.biologicalCompound == null) {
            throw new exception.ValidationException("sorry you need to provide a biologicalCompound!")
        }
        if (json.chemicalCompound == null) {
            throw new exception.ValidationException("sorry you need to provide a chemicalCompound!")
        }


        json = dropIds(json);

        Spectrum spectrum = new Spectrum()

        spectrum.spectrum = json.spectrum

        log.debug("inserting new spectra")

        //we build the metadata rather our self
        spectrum.metaData = [];

        //we build the tags our self
        spectrum.tags = [];

        log.info("valid: ${spectrum.validate()}")
        log.info(json)

        //add a submitter
        spectrum.submitter = submitterService.findOrCreateSubmitter(json.submitter)

        spectrum.biologicalCompound = compoundService.buildCompound(json.biologicalCompound);
        spectrum.chemicalCompound = compoundService.buildCompound(json.chemicalCompound)


        if (!spectrum.validate()) {
            log.error(spectrum.errors)
            throw new ValidationException("sorry was not able to persist spectra", spectrum.errors)
        }

        spectrum.save()

        if (json.tags) {
            def tags = json.tags

            //adding our tags
            tags.each {
                tagService.addTagTo(it.text, spectrum)
            }
        }

        metaDataPersistenceService.generateMetaDataFromJson(spectrum, json.metaData)
        spectrum.save()


        json.spectrum.split(" ").each { s ->
            def i = s.split(":")

            if (i.size() > 1) {
                double mass = Double.parseDouble(i[0])
                double intensity = Double.parseDouble(i[1])

                if (mass > 0 && intensity > 0) {
                    Ion ion = new Ion()
                    ion.spectrum = spectrum
                    ion.intensity = intensity
                    ion.mass = mass

                    ion.save()
                }
            }
        }

        //submit for validation
        SpectraValidationJob.triggerNow([spectraId: spectrum.id])

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

    /**
     * removes id objects from the json file, since we can't really reuse them
     * @param json
     * @return
     */
    private Map dropIds(Map json) {
        json.remove("id")

        json.remove("predictedCompound")

        json.entrySet().each {
            if (it instanceof Map) {
                dropIds(it)
            } else if (it instanceof Collection) {
                it.each {
                    if (it instanceof Map) {
                        dropIds()
                    }
                }
            }
        }
        return json
    }

}
