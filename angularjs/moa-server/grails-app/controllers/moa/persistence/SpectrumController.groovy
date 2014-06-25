package moa.persistence

import grails.rest.RestfulController
import moa.Compound
import moa.Spectrum
import moa.Submitter
import moa.Tag

class SpectrumController extends RestfulController<Spectrum> {


    static responseFormats = ['json']

    public SpectrumController() {
        super(Spectrum)
    }
    def beforeInterceptor = {
    }

    @Override
    protected Spectrum createResource(Map params) {
        log.info "building spectrum params: ${params}"

        Spectrum spectrum = null;
        Spectrum.withTransaction {
            spectrum = super.createResource(params)

            def biologicalNames = spectrum.biologicalCompound.names
            def chemicalNames = spectrum.chemicalCompound.names

            //we only care about refreshing the submitter by it's email address since it's unique
            spectrum.submitter = Submitter.findByEmailAddress(spectrum.submitter.emailAddress)

            //we need to ensure we don't double generate compound
            spectrum.biologicalCompound = Compound.findOrSaveWhere(inchiKey: spectrum.biologicalCompound.inchiKey)

            if (spectrum.biologicalCompound.names == null) {
                spectrum.biologicalCompound.names = [] as Set<String>
            }

            //merge new names
            biologicalNames.each { spectrum.biologicalCompound.names.add(it) }

            //we need to ensure we don't double generate compound
            spectrum.chemicalCompound = Compound.findOrSaveWhere(inchiKey: spectrum.chemicalCompound.inchiKey)

            if (spectrum.chemicalCompound.names == null) {
                spectrum.chemicalCompound.names = [] as Set<String>
            }

            //merge new names
            chemicalNames.each { spectrum.chemicalCompound.names.add(it) }


            def tags = []

            //adding our tags
            spectrum.tags.each {
                tags.add(Tag.findOrSaveWhere(text: it.text))
            }
            spectrum.tags = tags;
        }

        //spectrum is now ready to work on
        return spectrum;
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

            println(compound)
            return compound.spectra
        } else {
            return resource.list(params)

        }
    }
}
