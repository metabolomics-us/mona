import curation.CurationObject
import curation.CurationWorkflow
import curation.SubCurationWorkflow
import curation.actions.AddTagAction
import curation.actions.RemoveTagAction
import curation.rules.adduct.gcms.CompoundShouldBeDerivatizedRule
import curation.rules.adduct.gcms.ConfirmGCMSDerivatizationRule
import curation.rules.adduct.gcms.GCMSAdductCurationRule
import curation.rules.adduct.LCMSAdductCurationRule
import curation.rules.adduct.gcms.GCMSDerivatizationDoesntMatchCompound
import curation.rules.adduct.gcms.PredictGCMSCompoundRule
import curation.rules.adduct.gcms.PredictedMMinus15Rule
import curation.rules.compound.meta.CompoundComputeMetaDataRule
import curation.rules.compound.meta.DeletedComputedMetaDataRule
import curation.rules.compound.inchi.VerifyInChIKeyAndMolFileMatchRule
import curation.rules.instrument.GCMSSpectraIdentificationRule
import curation.rules.instrument.LCMSSpectraIdentificationRule
import curation.rules.meta.IsColumnValid
import curation.rules.meta.PercentageValueRule
import curation.rules.meta.ProvidedExactMassIsPossibleRule
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

    inchiKeyMatchesMolFile(VerifyInChIKeyAndMolFileMatchRule) { bean ->
        bean.autowire = 'byName'
    }

    deleteMetaDataRule(DeletedComputedMetaDataRule)

    deleteRuleBasedTagRule(RemoveComputedTagRule)

    compoundCurationWorkflow(CurationWorkflow) { workflow ->

        rules = [
                deleteMetaDataRule,
                deleteRuleBasedTagRule,
                computeCompoundValidationData,
                inchiKeyMatchesMolFile
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
 * GCMS Derivatization rules
 */
    gcmsDerivatizationRule(ConfirmGCMSDerivatizationRule)

    gcmsPredictDerivatizedCompoundRule(PredictGCMSCompoundRule) { bean ->
        bean.autowire = 'byName'
    }

    gcmsValidateChemicalCompound(GCMSDerivatizationDoesntMatchCompound){ bean ->
        predictGCMSCompoundRule = gcmsPredictDerivatizedCompoundRule

    }

    gcmsPredictMMinus15Rule(PredictedMMinus15Rule){ bean ->
        bean.autowire = 'byName'
        predictGCMSCompoundRule = gcmsPredictDerivatizedCompoundRule

    }

    gcmsCompoundShouldBeDerivatized(CompoundShouldBeDerivatizedRule) { bean ->
        bean.autowire = 'byName'
        predictGCMSCompoundRule = gcmsPredictDerivatizedCompoundRule
        maximumNoneDerivatizedMass = 600

    }

    /**
     * checks if the provided accurate mass is actuall possible
     */
    exactMassIsPossibleRule(ProvidedExactMassIsPossibleRule)

/**
 * set up subcuration workflow to check if it's an accurate mass spectra
 */
    isAccurateMassSpectra(SubCurationWorkflow, true) {
        rules = [
                preciseEnough
        ]

        successAction = new AddTagAction("accurate")
        failureAction = new RemoveTagAction("accurate")
    }

/**
 * set up metadata subcuration workflow
 */
    metadataCuration(SubCurationWorkflow, "suspect values", false, "metadata curation") {
        rules = [
                collisionEnergyPercentageRule,
                solventPercentageRule,
                flowGradientPercentageRule,
                isColumnValid,
                exactMassIsPossibleRule,
                gcmsDerivatizationRule
        ]
    }

/**
 * define our complete workflow here
 */
    spectraCurationWorkflow(CurationWorkflow) { workflow ->

        rules = [
                /**
                 * these rules should run first
                 */
                deleteRuleBasedTagRule,
                deleteMetaDataRule,
                convertSpectraToRelativeRule,
                lcmsSpectraIdentification,
                gcmsSpectraIdentification,
                /**
                 * order doesn't really matter here
                 */
                metadataCuration,
                isAccurateMassSpectra,
                isSpectraDirty,
                lcmsAdductCuration,
                gcmsAdductCuration,
                gcmsPredictDerivatizedCompoundRule,
                gcmsValidateChemicalCompound,
                gcmsPredictMMinus15Rule,
                gcmsCompoundShouldBeDerivatized,
                /**
                 * these rules should run last
                 */
                isAnnotatedSpectraRule

        ]
        //define and register our curation
    }

}
