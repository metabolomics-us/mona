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
    public synchronized Spectrum create(def json) {

       Spectrum spectrum = new Spectrum(json)

        	//we build the metadata rather our self
        	spectrum.metaData = [];

        	//we build the tags our self
        	spectrum.tags = [];

        	//we only care about refreshing the submitter by it's email address since it's unique
        	spectrum.submitter = Submitter.findByEmailAddress(spectrum.submitter.emailAddress)

        	//we need to ensure we don't double generate compound
        	spectrum.chemicalCompound = buildCompound(spectrum.chemicalCompound)
        	spectrum.biologicalCompound = buildCompound(spectrum.biologicalCompound)

        	if (spectrum.validate()){
           		spectrum.save(flush: true)
            		spectrum.lock()
            		def tags = json.tags

            		//adding our tags
            		tags.each {
				Tag tag = Tag.findOrSaveByText(it.text)
				tag.refresh()
                		spectrum.addToTags(tag)
            		}

        	} else {
        		log.warn(spectrum.errors)
        	}

        buildMetaData(spectrum, json.metaData)

        //spectrum is now ready to work on
        return spectrum;

    }

    /**
     * generates a correctly implemted metadata set
     * @param object - object to modify
     * @parm json - json definition of the metadata
     * @return
     */
    private synchronized void buildMetaData(Spectrum object, def json) {

        //remove existing metadata from the object

        json.each { current ->

            String metaDataName = metaDataDictionaryService.convertNameToBestMatch(current.name)

            MetaDataCategory category = categoryNameFinderService.findCategoryForMetaDataKey(metaDataName, current.category)

	    try{
                category.lock()
            }
            catch (e) {
                def newCat = MetaDataCategory.lock(category.id)
                category = newCat
            }
//            println("working on category: ${category}\t${category.validate()} with value: ${category.name}")

//            println("\t=>\tsave:${category}")

            MetaData metaData = MetaData.findOrSaveByNameAndCategory(metaDataName, category);
            category.addToMetaDatas(metaData)

//            println("\t==>\tworking on metadata: ${metaData}\t${metaData.validate()}")
            metaData.save(flush: true)
//            println("\t==>\tsave:${metaData}")
            category.save(flush: true)

            MetaDataValue metaDataValue = new StringMetaDataValue(stringValue: current.value.toString())
//MetaDataValueHelper.getValueObject(current.value)

            //if an unit is associated let's update it
            if (current.unit != null) {
                metaDataValue.unit = current.unit
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

            metaDataValue.save(flush: true)
        }
    }
/**
 * builds our internal compound object
 * @param compound
 * @return
 */
    private synchronized Compound buildCompound(Compound compound) {
        def names = compound.names

        //first get the compound we want
        def myCompound = Compound.findOrSaveByInchiKey(compound.inchiKey, [lock: true])

        //lets lock it
        myCompound = Compound.lock(myCompound.id)

        //merge new names
        names.each { name ->
            Name n = Name.findByNameAndCompound(name.name, myCompound)
            if (n != null) {
                myCompound.addToNames(new Name(name: name))
            }
        }

        myCompound.molFile = compound.molFile
        myCompound.save(flush: true)

        return myCompound;
    }

}
