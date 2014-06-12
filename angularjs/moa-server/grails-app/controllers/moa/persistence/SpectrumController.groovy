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

    @Override
    protected Spectrum createResource(Map params) {

        Spectrum spectrum = super.createResource(params)

        def biologicalNames = spectrum.biologicalCompound.names
        def chemicalNames = spectrum.chemicalCompound.names

        spectrum.biologicalCompound = Compound.findOrCreateWhere(inchiKey: spectrum.biologicalCompound.inchiKey).save(flush: true)

        if (spectrum.biologicalCompound.names == null) {
            spectrum.biologicalCompound.names = [] as Set<String>
        }

        biologicalNames.each { spectrum.biologicalCompound.names.add(it) }

        spectrum.chemicalCompound = Compound.findOrCreateWhere(inchiKey: spectrum.chemicalCompound.inchiKey).save(flush: true)

        if (spectrum.chemicalCompound.names == null) {
            spectrum.chemicalCompound.names = [] as Set<String>
        }

        chemicalNames.each { spectrum.chemicalCompound.names.add(it) }


        def tags = []

        spectrum.tags.each {
            tags.add(Tag.findOrCreateWhere(text: it.text))
        }
        spectrum.tags = tags;


        println(spectrum.biologicalCompound.inchiKey)
        println(spectrum.chemicalCompound.inchiKey)

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

        println "modified params: ${params}"
        params
    }
}
