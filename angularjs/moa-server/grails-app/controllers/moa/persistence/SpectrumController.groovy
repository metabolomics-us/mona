package moa.persistence

import grails.rest.RestfulController
import moa.Compound
import moa.Spectrum
import moa.Tag

class SpectrumController extends RestfulController<Spectrum> {


    static responseFormats = ['json']

    public SpectrumController() {
        super(Spectrum)
    }
    def beforeInterceptor = {
        println "Tracing action ${actionUri} - params: ${params}"
    }

    @Override
    protected Spectrum createResource(Map params) {
        log.info "building spectrum params: ${params}"

        Spectrum spectrum = super.createResource(params)

        def biologicalNames = spectrum.biologicalCompound.names
        def chemicalNames = spectrum.chemicalCompound.names

        spectrum.biologicalCompound = Compound.findOrSaveWhere(inchiKey: spectrum.biologicalCompound.inchiKey)

        if (spectrum.biologicalCompound.names == null) {
            spectrum.biologicalCompound.names = [] as Set<String>
        }
        biologicalNames.each { spectrum.biologicalCompound.names.add(it) }

        spectrum.chemicalCompound = Compound.findOrSaveWhere(inchiKey: spectrum.chemicalCompound.inchiKey)

        if (spectrum.chemicalCompound.names == null) {
            spectrum.chemicalCompound.names = [] as Set<String>
        }

        chemicalNames.each { spectrum.chemicalCompound.names.add(it) }


        def tags = []

        spectrum.tags.each {
            tags.add(Tag.findOrCreateWhere(text: it.text))
        }
        spectrum.tags = tags;


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
            println "returning spectra for compound: ${params.CompoundId}"
            return Spectrum.findByBiologicalCompoundOrChemicalCompound(Compound.get(params.CompoundId)
                    , Compound.get(params.CompoundId))
        } else {
            return resource.get(id)
        }
    }

    protected List<Spectrum> listAllResources(Map params) {
        if (params.CompoundId) {
            println "returning all spectra for compound: ${params.CompoundId}"
            Compound compound = Compound.get(params.CompoundId)

            println(compound)
            return compound.spectra
        } else {
            return resource.list(params)

        }
    }
}
