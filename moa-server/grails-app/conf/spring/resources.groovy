import util.caching.SpectrumKeyGenerator
import validation.ValidationWorkflow
import validation.rules.instrument.GCMSSpectraIdentificationRule
import validation.rules.instrument.LCMSSpectraIdentificationRule
import validation.rules.meta.PercentageValueRule
import validation.rules.spectra.IsAccurateMassSpectraRule
import validation.rules.spectra.MassSpecIsPreciseEnoughRule

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
    collisionEnergyPercentageRule(PercentageValueRule,"collision energy"){

        minPercentage = 0
        maxPercentage = 100
    }

    /**
     * tests the preccssion of the ions in a mass spec
     */
    preciseEnough(MassSpecIsPreciseEnoughRule){ spec ->
        minPrecission = 3
    }

    /**
     * complex check to see if it's an accurate mass spectra
     */
    isAccurateMassSpectra(IsAccurateMassSpectraRule){


        rules = [
                preciseEnough
        ]
    }
    /**
     * define our complete workflow here
     */
    validationWorkflow(ValidationWorkflow) { workflow ->

        rules = [
                lcmsSpectraIdentification,
                gcmsSpectraIdentification,
                isAccurateMassSpectra,
                collisionEnergyPercentageRule
        ]
        //define and register our validation
    }

}
