import curation.rules.spectra.ConvertMassspectraToRelativeSpectraRule
import curation.rules.spectra.IsCleanSpectraRule
import util.caching.SpectrumKeyGenerator
import curation.CurationWorkflow
import curation.CurationWorkflow
import curation.rules.instrument.GCMSSpectraIdentificationRule
import curation.rules.instrument.LCMSSpectraIdentificationRule
import curation.rules.meta.PercentageValueRule
import curation.rules.spectra.IsAccurateMassSpectraRule
import curation.rules.spectra.MassSpecIsPreciseEnoughRule

// Place your Spring DSL code here
beans = {
    rest(grails.plugins.rest.client.RestBuilder)

    cacheKey(SpectrumKeyGenerator)

    /**
     * define some rules here
     */

    lcmsSpectraIdentification(LCMSSpectraIdentificationRule)
    gcmsSpectraIdentification(GCMSSpectraIdentificationRule)

    /**
     * limit our collision energy in case of percentages to under 100 and over 0
     */
    collisionEnergyPercentageRule(PercentageValueRule, "collision energy") {

        minPercentage = 0
        maxPercentage = 100
    }

    /**
     * flow gradiant percentage rule
     */
    flowGradientPercentageRule(PercentageValueRule, "flow gradient") {

        minPercentage = 0
        maxPercentage = 100
    }

    /**
     * solvent percentage rule
     */
    solventPercentageRule(PercentageValueRule, "solvent") {

        minPercentage = 0
        maxPercentage = 100
    }

    /**
     * tests the preccssion of the ions in a mass spec
     */
    preciseEnough(MassSpecIsPreciseEnoughRule) { spec ->
        minPrecission = 3
    }

    /**
     * complex check to see if it's an accurate mass spectra
     */
    isAccurateMassSpectra(IsAccurateMassSpectraRule) {


        rules = [
                preciseEnough
        ]
    }

    /**
     * spectras should always be relative and not absolute
     */
    convertSpectraToRelativeRule(ConvertMassspectraToRelativeSpectraRule)

    /**
     * is spectra dirty
     */
    isSpectraDirty(IsCleanSpectraRule){
        noisePercentage = 2
        percentOfSpectraIsNoise = 80
    }
    /**
     * define our complete workflow here
     */
    curationWorkflow(CurationWorkflow) { workflow ->

        rules = [
                lcmsSpectraIdentification,
                gcmsSpectraIdentification,
                isAccurateMassSpectra,
                collisionEnergyPercentageRule,
                solventPercentageRule,
                flowGradientPercentageRule,
                convertSpectraToRelativeRule,
                isSpectraDirty
        ]
        //define and register our curation
    }

}
