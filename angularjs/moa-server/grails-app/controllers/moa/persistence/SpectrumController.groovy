package moa.persistence

import grails.rest.RestfulController
import moa.*
import moa.meta.DoubleValue
import moa.meta.IntegerValue

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

        buildMetaData(spectrum.metaData, spectrum)

        //spectrum is now ready to work on
        return spectrum;
    }

    /**
     * generates a correctly implemted metadata set
     * @param metaData
     * @return
     */
    private void buildMetaData(Set<MetaData> metaData, Spectrum spectrum) {

        Set<MetaData> result = new HashSet<>()

        for (MetaData m : metaData) {
            MetaData c = MetaData.findOrSaveByName(m.name)

            log.info(m)

            c.name = m.name


            m.values.each { Value v ->

                String value = v.value.toString()

                //try catch approach is the fastest, even if it's uglier than regex
                try {
                    c.addToValues(new IntegerValue(value: Integer.parseInt(value)))
                    log.info("it's an integer value: ${value}")
                }
                catch (NumberFormatException e) {
                    try {
                        c.addToValues(new DoubleValue(value: Double.parseDouble(value)))
                        log.info("it's a double value: ${value}")
                    }
                    catch (NumberFormatException x) {

                        c.addToValues(new moa.meta.StringValue(value: value))
                        log.info("it's a string value: ${value}")
                    }
                }


            };



            log.info("trying to save value: " + c + " with content ${c.values}");
            result.add(c)
        }

        spectrum.metaData.clear();

        result.each {
            spectrum.addToMetaData(it)
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
