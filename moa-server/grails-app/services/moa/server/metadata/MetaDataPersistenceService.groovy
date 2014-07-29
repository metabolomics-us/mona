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

@Transactional
class MetaDataPersistenceService {

    MetaDataDictionaryService metaDataDictionaryService

    CategoryNameFinderService categoryNameFinderService

    /**
     * generates our required metadata based on the json array of metadata
     * @param object
     * @param json a json array containing metadata objects
     */
    public void generateMetaDataFromJson(SupportsMetaData object, def json) {

        log.debug("generating meta data")
        //remove existing metadata from the object

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

            //we need a check for duplicates!

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
