package moa.server.metadata

import grails.transaction.Transactional
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
     * generates our required metadata based on the json array of metadata
     * @param object
     * @param json a json array containing metadata objects
     */
    public void generateMetaDataFromJson(SupportsMetaData object, def json) {

        log.debug("generating meta data")
        //remove existing metadata from the object
        object.refresh()
        json.each { Map current ->
            generateMetaDataObject(object, current)
        }
    }

    /**
     * associates the defined metadata in the object with the associated object
     * @param object
     * @param current
     */
    public void generateMetaDataObject(SupportsMetaData object, Map current) {
        log.debug("received ${object} and map: ${current}")

        if (!metadataFilters.accept(current.name, current.value)) {
            log.info("metadata '${current.name}' with value  '${current.value}' rejected at filters...")
            return
        }

        String metaDataName = metaDataDictionaryService.convertNameToBestMatch(current.name)

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
        MetaDataCategory category = categoryNameFinderService.findCategoryForMetaDataKey(metaDataName, current.category)


        log.debug("generating metadata object...")
        MetaData metaData = MetaData.findOrSaveByNameAndCategory(metaDataName, category);
        category.addToMetaDatas(metaData)

        metaData.save()
        category.save()

        log.debug("generating metadata value object...")
        MetaDataValue metaDataValue = new StringMetaDataValue(stringValue: current.value.toString())
//MetaDataValueHelper.getValueObject(current.value)

        if (current.computed != null && current.computed) {
            metaDataValue.computed = true
        } else {
            metaDataValue.computed = false
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

            metaData.addToValue(metaDataValue)
            object.addToMetaData(metaDataValue)

        } catch (Exception e) {
            log.warn("ignored metadata, due to an invalid type exception: ${e.message}", e);
        }

        metaDataValue.save()

        log.debug("done")
    }
}
