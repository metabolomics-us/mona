package moa.server.metadata

import exception.ConfigurationError
import grails.transaction.Transactional
import grails.validation.ValidationException
import moa.MetaData
import moa.MetaDataCategory
import moa.MetaDataValue
import moa.SupportsMetaData
import moa.meta.BooleanMetaDataValue
import moa.meta.DoubleMetaDataValue
import moa.meta.StringMetaDataValue
import moa.server.CategoryNameFinderService
import moa.server.MetaDataDictionaryService
import persistence.metadata.filter.Filters
import persistence.metadata.filter.unit.Converters

@Transactional
class MetaDataPersistenceService {

    MetaDataDictionaryService metaDataDictionaryService

    CategoryNameFinderService categoryNameFinderService

    Filters metadataFilters

    Converters metadataValueConverter

    /**
     * deletes a metadata value object
     * @param value our value
     * @param deleteNow should it deleted this instance or just marked as invisible
     */
    public void removeMetaDataValue(MetaDataValue value, def deleteNow = false) {
        //if(value.isDirty()){
            value.refresh()
        //}
        log.info("deleting metadata value object: ${value}")

        if(deleteNow) {
            log.info("deleting: ${value}")


            value.merge()
            value.metaData.removeFromValue(value)
            value.owner.removeFromMetaData(value)

            value.delete()
        }
        else {
            value.deleted = true
            value.save()
        }
    }
    /**
     * generates our required metadata based on the json array of metadata
     * @param object
     * @param json a json array containing metadata objects
     */
    public void generateMetaDataFromJson(SupportsMetaData object, def json) {

        log.debug("generating meta data")
        //remove existing metadata from the object
        //object.refresh()
        json.each { Map current ->
            generateMetaDataObject(object, current)
        }
    }

    /**
     * associates the defined metadata in the object with the associated object
     * @param object object to attach the data to
     * @param current current metadata value
     * @param replace should we we replace already rexisting data
     */
    public void generateMetaDataObject(SupportsMetaData object, Map current, boolean replace = false) {
        log.debug("received ${object} and map: ${current}")

        if (current.name == null || current.value == null) {
            log.warn("received null data for some reason, object was ${object}")
            return
        }

        if (current.name.toString().length() == 0 || current.value.toString().trim().length() == 0) {
            log.warn("received null data for some reason, object was ${object}")
            return
        }

        if (metadataFilters == null) {
            throw new ConfigurationError("sorry it looks like the filters were not injected!")
        }
        if (!metadataFilters.accept(current.name, current.value)) {
            log.info("metadata '${current.name}' with value  '${current.value}' rejected at filters...")
            return
        }

        String metaDataName = metaDataDictionaryService.convertNameToBestMatch(current.name)

        //delete old object
        if (replace) {

            log.info("removing old objects, to avoid duplication")
            def toDelete = []

            object.metaData.each { MetaDataValue m ->
                if (m.name == metaDataName) {
                    toDelete.add(m)
                }
            }

            toDelete.each { MetaDataValue v ->
                removeMetaDataValue(v)
            }
        }

        //calculate our units
        Map calculatedValue = metadataValueConverter.convert(metaDataName, current.value.toString())

        if (!calculatedValue.isEmpty()) {
            //and assign them
            if (calculatedValue.unit != null) {
                current.unit = calculatedValue.unit
            }
            if (calculatedValue.value != null) {
                current.value = calculatedValue.value
            }
        }

        MetaData metaData = createMetadataObject(metaDataName, current)

        log.debug("generating metadata value object...")
        MetaDataValue metaDataValue = new StringMetaDataValue(stringValue: current.value.toString())

        if (current.computed != null && current.computed) {
            metaDataValue.computed = true
        } else {
            metaDataValue.computed = false
        }

        if(current.url != null){
            metaDataValue.url = current.url
        }

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

            //we need a check for duplicates!

            metaDataValue.metaData = metaData
            metaDataValue.owner = object

            metaData.save()
            object.save()

        }
        catch (exception.ValidationException e) {
            throw e;
        }
        catch (Exception e) {
            log.warn("ignored metadata, due to an invalid type exception: ${e.message}", e);
        }


        if (!metaData.validate()) {
            throw new ValidationException("sorry a none recoverable error occurred, while creating a meta data object", metaData.errors)
        }

        if (!metaDataValue.validate()) {
            throw new ValidationException("sorry a none recoverable error occurred, while creating a meta data value object ($metaDataValue.name - $metaDataValue.value)", metaDataValue.errors)

        }
        metaDataValue.save()

        log.debug("done")
    }

    def MetaData createMetadataObject(String metaDataName, Map current) {
        MetaDataCategory category = categoryNameFinderService.findCategoryForMetaDataKey(metaDataName, current.category)


        log.debug("generating metadata object...")
        MetaData metaData = MetaData.findOrSaveByName(metaDataName);

        if (metaData.category == null) {
            category.addToMetaDatas(metaData)
            //metaData.category = category
        }
        metaData.save()
        category.save()
        metaData
    }
}
