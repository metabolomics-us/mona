package moa.persistence

import grails.rest.RestfulController
import moa.*
import moa.meta.BooleanMetaDataValue
import moa.meta.DoubleMetaDataValue
import moa.server.CategoryNameFinderService
import moa.server.MetaDataDictionaryService
import util.MetaDataValueHelper

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

        def chemicalNames = spectrum.chemicalCompound.names

        //we only care about refreshing the submitter by it's email address since it's unique
        spectrum.submitter = Submitter.findByEmailAddress(spectrum.submitter.emailAddress)

        //we need to ensure we don't double generate compound
        spectrum.chemicalCompound = buildCompound(spectrum.chemicalCompound)

        spectrum.biologicalCompound = buildCompound(spectrum.biologicalCompound)


        def tags = []

        //adding our tags
        spectrum.tags.each {
            tags.add(Tag.findOrSaveWhere(text: it.text))
        }
        spectrum.tags = tags;


        //we build the metadata rather our self
        spectrum.metaData = [];

        //actually assemble them
        buildMetaData(spectrum, request.JSON.metaData)

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

        //remove existing metadata from the object

        json.each { current ->

            String metaDataName = metaDataDictionaryService.convertNameToBestMatch(current.name)

            MetaData metaData = MetaData.findOrSaveByName(metaDataName);

            //associated our default category, if none exist
            if (metaData.category == null) {

                String name = current.category

                //check if no explicit category was provided
                if (name == null || name.length() == "") {

                    //check if we alreay have a preferred category name for this metadata key
                    name = categoryNameFinderService.findCategoryNameForMetaDataKey(metaData.name)

                }

                MetaDataCategory category = MetaDataCategory.findOrSaveByName(name)
                category.addToMetaDatas(metaData)
                metaData.category = category

            }
            MetaDataValue metaDataValue = MetaDataValueHelper.getValueObject(current.value)

            if (metaDataValue instanceof DoubleMetaDataValue) {
                if (metaData.type == null) {
                    metaData.type = "double";
                } else {
                    if (!metaData.type.equals("double")) {
                        throw new Exception("metaData '${metaData.name}' needs to be of type 'double'");
                    }
                }
            } else if (metaDataValue instanceof BooleanMetaDataValue) {
                if (metaData.type == null) {
                    metaData.type = "boolean";
                } else {
                    if (!metaData.type.equals("boolean")) {
                        throw new Exception("metaData '${metaData.name}' needs to be of type 'boolean'");
                    }
                }
            } else {
                if (metaData.type == null) {
                    metaData.type = "string";
                } else {
                    if (!metaData.type.equals("string")) {
                        throw new Exception("metaData '${metaData.name}' needs to be of type 'string'");
                    }
                }
            }

            metaData.addToValue(metaDataValue)
            metaDataValue.spectrum = object
            object.addToMetaData(metaDataValue)
        }

    }
/**
 * builds our internal compound object
 * @param compound
 * @return
 */
    private Compound buildCompound(Compound compound) {
        def names = compound.names

        def myCompound = Compound.findOrSaveByInchiKey(compound.inchiKey)

        myCompound.save(flush: true)

        if (myCompound.names == null) {
            myCompound.names = new HashSet<Name>();
        }
        //merge new names
        names.each { name ->
            myCompound.addToNames(Name.findOrSaveByName(name.name))
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
