import curation.CurationObject
import curation.CurationWorkflow
import curation.SubCurationWorkflow
import curation.rules.adduct.GCMSAdductCurationRule
import curation.rules.adduct.LCMSAdductCurationRule
import curation.rules.compound.meta.CompoundComputeMetaDataRule
import curation.rules.compound.meta.DeletedComputedMetaDataRule
import curation.rules.instrument.GCMSSpectraIdentificationRule
import curation.rules.instrument.LCMSSpectraIdentificationRule
import curation.rules.meta.IsColumnValid
import curation.rules.meta.PercentageValueRule
import curation.rules.spectra.ConvertMassspectraToRelativeSpectraRule
import curation.rules.spectra.IsAnnotatedSpectraRule
import curation.rules.spectra.IsCleanSpectraRule
import curation.rules.spectra.MassSpecIsPreciseEnoughRule
import curation.rules.tag.RemoveComputedTagRule
import grails.spring.BeanBuilder
import util.caching.SpectrumKeyGenerator


// Place your Spring DSL code here
beans = {


    rest(grails.plugins.rest.client.RestBuilder)

    cacheKey(SpectrumKeyGenerator)

    /**
     * compound curation workflow
     */

    /**
     * computes the compound validation data
     */
    computeCompoundValidationData(CompoundComputeMetaDataRule) { bean ->
        bean.autowire = 'byName'
    }

    deleteMetaDataRule(DeletedComputedMetaDataRule)


    compoundCurationWorkflow(CurationWorkflow) { workflow ->

        rules = [
                deleteMetaDataRule,
                computeCompoundValidationData
        ]
    }

/**
 * Spectra curation workflow
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
 * tests the precision of the ions in a mass spec
 */
    preciseEnough(MassSpecIsPreciseEnoughRule) { spec ->
        minPrecision = 3
    }

/**
 * does the spectra has any annotations
 */
    isAnnotatedSpectraRule(IsAnnotatedSpectraRule)

/**
 * spectra should always be relative and not absolute
 */
    convertSpectraToRelativeRule(ConvertMassspectraToRelativeSpectraRule)

/**
 * is spectra dirty
 */
    isSpectraDirty(IsCleanSpectraRule) {
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
    lcmsAdductCuration(LCMSAdductCurationRule) { bean ->
        bean.autowire = 'byName'
        toleranceInDalton = 0.5
        minAdducts = 1
    }

/**
 * verify that a lcms spectrum has valid adducts
 */
    gcmsAdductCuration(GCMSAdductCurationRule) { bean ->
        bean.autowire = 'byName'
        toleranceInDalton = 1
        minAdducts = 1
    }

/**
 * set up subcuration workflow to check if it's an accurate mass spectra
 */
    isAccurateMassSpectra(SubCurationWorkflow, "accurate", true, "accurate mass validation") {
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

    deleteRuleBasedTagRule(RemoveComputedTagRule)

/**
 * define our complete workflow here
 */
    spectraCurationWorkflow(CurationWorkflow) { workflow ->

        rules = [
                deleteRuleBasedTagRule,
                deleteMetaDataRule,
                // Metadata curation
                metadataCuration,

                // Tagging curation steps
                lcmsSpectraIdentification,
                gcmsSpectraIdentification,
                isAccurateMassSpectra,
                convertSpectraToRelativeRule,
                isSpectraDirty,
                isAnnotatedSpectraRule,
                lcmsAdductCuration,
                gcmsAdductCuration

        ]
        //define and register our curation
    }

}
