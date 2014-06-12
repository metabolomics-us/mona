package moa.persistence

import grails.rest.RestfulController
import moa.Compound
import moa.Spectrum
import moa.Tag

class SpectrumController extends RestfulController<Spectrum> {

    static responseFormats = ['json']

    def index() {}

    public SpectrumController() {
        super(Spectrum)
    }

    @Override
    protected Spectrum createResource(Map params) {

        Spectrum spectrum = super.createResource(params)

        spectrum.biologicalCompound = Compound.findOrCreateWhere(inchiKey: spectrum.biologicalCompound.inchiKey)
        spectrum.chemicalCompound = Compound.findOrCreateWhere(inchiKey: spectrum.chemicalCompound.inchiKey)


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

        println "modified params: ${params}"
        params
    }
}
