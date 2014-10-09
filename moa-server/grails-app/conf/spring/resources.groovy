import curation.rules.adduct.IsValidLCMSSpectrum
import curation.rules.meta.IsColumnValid
import curation.rules.spectra.ConvertMassspectraToRelativeSpectraRule
import curation.rules.spectra.IsAnnotatedSpectraRule
import curation.rules.spectra.IsAnnotatedSpectraRule
import curation.rules.spectra.IsCleanSpectraRule
import util.caching.SpectrumKeyGenerator
import curation.CurationWorkflow
import curation.CurationWorkflow
import curation.rules.instrument.GCMSSpectraIdentificationRule
import curation.rules.instrument.LCMSSpectraIdentificationRule
import curation.rules.meta.PercentageValueRule
import curation.SubCurationWorkflow
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
     * does the spectra has any annotations
     */
    isAnnotatedSpectraRule(IsAnnotatedSpectraRule)

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
     * is column metadata valid
     */
    isColumnValid(IsColumnValid)

    /**
     * verify that a lcms spectrum has valid adducts
     */
    isValidLCMSSpectrum(IsValidLCMSSpectrum) {
         TOL = 0.5
         N_ADDUCTS = 1
     }



    /**
     * set up subcuration workflow to check if it's an accurate mass spectra
     */
    isAccurateMassSpectra(SubCurationWorkflow, "accurate", true, "accruate mass validation") {
        rules = [
                preciseEnough
        ]
    }

    /**
     * set up metadata subcuration workflow
     */
    metadataCuration(SubCurationWorkflow, "suspect values", false, "metadata curation") {
        rules = [
                collisionEnergyPercentageRule,
                solventPercentageRule,
                flowGradientPercentageRule,
                isColumnValid
        ]
    }

    /**
     * define our complete workflow here
     */
    curationWorkflow(CurationWorkflow) { workflow ->

        rules = [
                // Metadata curation
                metadataCuration,

                // Tagging curation steps
                lcmsSpectraIdentification,
                gcmsSpectraIdentification,
                isAccurateMassSpectra,
                convertSpectraToRelativeRule,
                isSpectraDirty,
                isAnnotatedSpectraRule,
                isValidLCMSSpectrum,

        ]
        //define and register our curation
    }
}
