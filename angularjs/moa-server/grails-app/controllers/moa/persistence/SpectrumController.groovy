package moa.persistence

import grails.rest.RestfulController
import grails.transaction.Transactional
import moa.*
import moa.meta.BooleanMetaDataValue
import moa.meta.DoubleMetaDataValue
import moa.meta.StringMetaDataValue
import moa.server.CategoryNameFinderService
import moa.server.MetaDataDictionaryService

class SpectrumController extends RestfulController<Spectrum> {
    static responseFormats = ['json']

    MetaDataDictionaryService metaDataDictionaryService

    CategoryNameFinderService categoryNameFinderService

    def beforeInterceptor = {
        log.info(params)
    }

    public SpectrumController() {
        super(Spectrum)
    }

    protected Map getParametersToBind() {
        log.info(params)

        if (request.JSON) {
            params.putAll(
                    request.JSON)
        }

        params
    }

    @Override
    protected Spectrum createResource(Map params) {

        Spectrum spectrum = super.createResource(params)

        //we build the metadata rather our self
        spectrum.metaData = [];

        //we build the tags our self
        spectrum.tags = [];

        //we only care about refreshing the submitter by it's email address since it's unique
        spectrum.submitter = Submitter.findByEmailAddress(spectrum.submitter.emailAddress)

        //we need to ensure we don't double generate compound
        spectrum.chemicalCompound = buildCompound(spectrum.chemicalCompound)

        spectrum.biologicalCompound = buildCompound(spectrum.biologicalCompound)



        if(spectrum.validate()) {
            spectrum.save(flush: true)

            def tags = request.JSON.tags

            //adding our tags
            tags.each {
                spectrum.addToTags(Tag.findOrCreateByText(it.text))
            }

            //actually assemble them
            buildMetaData(spectrum, request.JSON.metaData)
        }
        else{
            log.warn(spectrum.errors)
        }

        //spectrum is now ready to work on
        return spectrum;
    }

    /**
     * generates a correctly implemted metadata set
     * @param object - object to modify
     * @parm json - json definition of the metadata
     * @return
     */
    @Transactional
    private void buildMetaData(Spectrum object, def json) {

        //remove existing metadata from the object

        json.each { current ->

            String metaDataName = metaDataDictionaryService.convertNameToBestMatch(current.name)

            MetaData metaData = MetaData.findOrCreateByName(metaDataName);

            //associated our default category, if none exist
            if (metaData.category == null) {

                String name = current.category

                //check if no explicit category was provided
                if (name == null || name.length() == "") {

                    //check if we alreay have a preferred category name for this metadata key
                    name = categoryNameFinderService.findCategoryNameForMetaDataKey(metaData.name)

                }

                MetaDataCategory category = MetaDataCategory.findOrSaveByName(name)

                try {
                    category.lock()
                }
                catch (Exception e) {

                    def newCat = MetaDataCategory.lock(category.id);
                    category = newCat
                }

                category.addToMetaDatas(metaData)

            }
            MetaDataValue metaDataValue = new StringMetaDataValue(stringValue: current.value.toString())//MetaDataValueHelper.getValueObject(current.value)

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
        }

    }
/**
 * builds our internal compound object
 * @param compound
 * @return
 */
    @Transactional
    private Compound buildCompound(Compound compound) {
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

    protected Spectrum queryForResource(Serializable id) {

        def criteria = Spectrum.createCriteria()

        return criteria.get {
            if (params.CompoundId) {
                eq("id", Long.parseLong(id.toString()))
            }
        }
    }

    /**
     * dynamic query methods to deal with different url mappings based on mapping ids
     * @param params
     * @return
     */
    protected List<Spectrum> listAllResources(Map params) {

        log.info("params: ${params}")

        return Spectrum.createCriteria().list(params) {

            //if a compound is specified
            if (params.CompoundId) {
                log.info("query by compounds: ${params.CompoundId}")

                biologicalCompound {
                    eq("id", Long.parseLong(params.CompoundId))
                }

                or {
                    chemicalCompound {
                        eq("id", Long.parseLong(params.CompoundId))
                    }
                }
            }
            //if a category is specified
            if (params.MetaDataId) {

                log.info("query by meta data: ${params.MetaDataId}")
                metaData {
                    eq("id", Long.parseLong(params.MetaDataId))

                    //if we got a category as well
                }
            }
        }
    }


}
