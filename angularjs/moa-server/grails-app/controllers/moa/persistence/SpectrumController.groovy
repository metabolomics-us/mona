package moa.persistence

import grails.rest.RestfulController
import moa.*
import moa.meta.BooleanMetaDataValue
import moa.meta.DoubleMetaDataValue
import moa.meta.IntegerMetaDataValue
import moa.meta.StringMetaDataValue

class SpectrumController extends RestfulController<Spectrum> {


    static responseFormats = ['json']

    public SpectrumController() {
        super(Spectrum)
    }
    def beforeInterceptor = {
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
        object.metaData.clear()

        json.each { current ->
            MetaData metaData = MetaData.findOrSaveByName(current.name);

            //associated our default category, if none exist
            if(metaData.category == null){
                MetaDataCategory category = MetaDataCategory.findOrSaveByName('none associated')
                category.addToMetaDatas(metaData)
                metaData.category = category

            }
            MetaDataValue metaDataValue = null
            try {

                Integer value = Integer.parseInt(current.value)
                metaDataValue = (new IntegerMetaDataValue(integerValue: value))

            } catch (NumberFormatException e) {
                try {
                    Double value = Double.parseDouble(current.value)
                    metaDataValue = (new DoubleMetaDataValue(doubleValue: value))

                } catch (NumberFormatException ex) {
                    if (current.value.toString().toLowerCase().trim() == "true") {
                        metaDataValue = (new BooleanMetaDataValue(booleanValue: true))

                    } else if (current.value.toString().toLowerCase().trim() == "false") {
                        metaDataValue = (new BooleanMetaDataValue(booleanValue: false))

                    } else {
                        metaDataValue = (new StringMetaDataValue(stringValue: current.value))

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
/**
 * otherwise grails won't populate the json fields
 * @return
 */
    protected Map getParametersToBind() {
        if (request.JSON) {
            params.putAll(
                    request.JSON)
        }

        params
    }

    protected Spectrum queryForResource(Serializable id) {
        if (params.CompoundId) {
            return Spectrum.findByBiologicalCompoundOrChemicalCompound(Compound.get(params.CompoundId)
                    , Compound.get(params.CompoundId))
        } else {
            return resource.get(id)
        }
    }

    protected List<Spectrum> listAllResources(Map params) {
        if (params.CompoundId) {
            Compound compound = Compound.get(params.CompoundId)

            return Spectrum.findAllByBiologicalCompoundOrChemicalCompound(compound, compound)
        } else {
            return resource.list(params)

        }
    }

}
