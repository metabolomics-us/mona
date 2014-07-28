package moa.server

import moa.*
import moa.meta.BooleanMetaDataValue
import moa.meta.DoubleMetaDataValue
import moa.meta.StringMetaDataValue

class SpectraPersistenceService {

    MetaDataDictionaryService metaDataDictionaryService

    CategoryNameFinderService categoryNameFinderService

    /**
     * creates a new spectrum and saves it in the database
     * @param params
     * @return
     */
    public synchronized Spectrum create(Map json) {

        Spectrum spectrum = new Spectrum(json)

        log.debug("inserting new spectra: ${spectrum.spectrum}")

        //we build the metadata rather our self
        spectrum.metaData = [];

        //we build the tags our self
        spectrum.tags = [];

        //we only care about refreshing the submitter by it's email address since it's unique
        spectrum.submitter = Submitter.findByEmailAddress(spectrum.submitter.emailAddress)

        //we need to ensure we don't double generate compound
        spectrum.chemicalCompound = buildCompound(spectrum.chemicalCompound)
        spectrum.biologicalCompound = buildCompound(spectrum.biologicalCompound)

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

        buildMetaData(spectrum, json.metaData)

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
    private void buildMetaData(Spectrum object, def json) {

        log.debug("generating meta data")
        //remove existing metadata from the object

        json.each { current ->

            String metaDataName = metaDataDictionaryService.convertNameToBestMatch(current.name)

            MetaDataCategory category = categoryNameFinderService.findCategoryForMetaDataKey(metaDataName, current.category)


            MetaData metaData = MetaData.findOrSaveByNameAndCategory(metaDataName, category);
            category.addToMetaDatas(metaData)

            metaData.save()
            category.save()

            MetaDataValue metaDataValue = new StringMetaDataValue(stringValue: current.value.toString())
//MetaDataValueHelper.getValueObject(current.value)

            //if an unit is associated let's update it
            if (current.unit != null) {
                metaDataValue.unit = current.unit

                if (!metaData.requiresUnit) {
                    metaData.requiresUnit = true
                }
            }

            try {
                if (metaDataValue instanceof DoubleMetaDataValue) {
                    if (metaData.type == null) {
                        metaData.type = "double";
                    } else {
                        if (!metaData.type.equals("double")) {
                            throw new Exception("metaData '${metaData.name}' needs to be of type 'double', but is of type: ${metaData.type}");
                        }
                    }
                } else if (metaDataValue instanceof BooleanMetaDataValue) {
                    if (metaData.type == null) {
                        metaData.type = "boolean";
                    } else {
                        if (!metaData.type.equals("boolean")) {
                            throw new Exception("metaData '${metaData.name}' needs to be of type 'boolean', but is of type: ${metaData.type}");
                        }
                    }
                } else {
                    if (metaData.type == null) {
                        metaData.type = "string";
                    } else {
                        if (!metaData.type.equals("string")) {
                            throw new Exception("metaData '${metaData.name}' needs to be of type 'string', but is of type: ${metaData.type}");
                        }
                    }
                }

                metaData.addToValue(metaDataValue)
                object.addToMetaData(metaDataValue)

            } catch (Exception e) {
                log.warn("ignored metadata, due to an invalid type exception: ${e.message}", e);
            }

//            println("\t===>\tworking on value: ${metaDataValue}")

            log.debug("${metaDataValue.category}:${metaDataValue.name}:${metaDataValue.value}:${metaDataValue.unit}")

            metaDataValue.save()
        }
    }
/**
 * builds our internal compound object
 * @param compound
 * @return
 */
    private Compound buildCompound(Compound compound) {

        def names = compound.names

        log.debug("trying to generate compound: ${compound.inchiKey}")

        //first get the compound we want
        def myCompound = Compound.findByInchiKey(compound.inchiKey)

        if (!myCompound) {
            Compound.withTransaction {
                myCompound = new Compound(inchiKey: compound.inchiKey)
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
        myCompound.save()


        return myCompound;


    }

}
