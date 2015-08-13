/**
 * defines our workflow for spectra curation tasks
 */


import curation.CurationWorkflow
import curation.SubCurationWorkflow
import curation.rules.adduct.lcms.LCMSAdductCurationRule
import curation.rules.adduct.gcms.CompoundShouldBeDerivatizedRule
import curation.rules.adduct.gcms.ConfirmGCMSDerivatizationRule
import curation.rules.adduct.gcms.GCMSAdductCurationRule
import curation.rules.adduct.gcms.GCMSDerivatizationDoesntMatchCompound
import curation.rules.adduct.gcms.PredictGCMSCompoundRule
import curation.rules.adduct.gcms.PredictedMMinus15Rule
import curation.rules.instrument.GCMSSpectraIdentificationRule
import curation.rules.instrument.LCMSSpectraIdentificationRule
import curation.rules.meta.DerivativeTypeSpelling
import curation.rules.meta.DropNoneNecessaryMetaDataRule
import curation.rules.meta.IsColumnValid
import curation.rules.meta.PercentageValueRule
import curation.rules.meta.ProvidedExactMassIsPossibleRule
import curation.rules.meta.RemapMetadataNames
import curation.rules.meta.lipidblast.LipidBlastAquisitionModeDetectionRule
import curation.rules.meta.lipidblast.LipidBlastMSMSDetectionRule
import curation.rules.spectra.CalculateMassAccuracyRule
import curation.rules.spectra.ConvertMassspectraToRelativeSpectraRule
import curation.rules.spectra.ExactMassIsCorrectRule
import curation.rules.spectra.GenerateHashKeyRule
import curation.rules.spectra.IsAnnotatedSpectraRule
import curation.rules.spectra.IsCleanSpectraRule
import curation.rules.spectra.IsDuplicatedSpectraRule
import curation.rules.spectra.MassSpecIsPreciseEnoughRule
import curation.rules.spectra.RemoveIdenticalSpectraRule
import curation.rules.spectra.RemoveTinyIonRule
import curation.rules.tree.GenerateFragmentationTreesRuleForMassBank
import static util.MetaDataFieldNames.*

beans {

    remapMetadataNames(RemapMetadataNames) { bean ->
        bean.autowire = 'byName'
        mapping = [
                "precursormz"             : PRECURSOR_MASS,
                "adductionname"           : PRECURSORTYPE,
                "retentiontime"           : RETENTION_TIME,
                "transfarline temperature": TRANSFER_LINE_TEMPERATURE,
                "mslevel"                 : MS_LEVEL,
                "ionmode"                 : IONMODE,
                "compoundclass"           : COMPOUND_CLASS,
                "author"                  : AUTHORS,
                "adduct"                  : ADDUCT,
                "adductionname"           : ADDUCT,
                "adduct ion name"         : ADDUCT,
                "column"                  : COLUMN_NAME,
                "derivative form"         : DERIVATIVE_SUM_FORMULA,
                "molecule formula"        : MOLECULAR_SUM_FORMULA,
                "molecular formula"       : MOLECULAR_SUM_FORMULA,
                "mz exact"                : EXACT_MASS,
                "ms type"                 : MS_LEVEL,
                "submituser"              : AUTHORS,
                "source instrument"       : INSTRUMENT,
                "ri"                      : RETENTION_INDEX,
                "pi"                      : AUTHORS

        ]
    }

    removeTinyIonRule(RemoveTinyIonRule) { bean ->
        bean.autowire = 'byName'
    }

    //generate our hashkeys for unique spectra identification
    generateHashKeyRule(GenerateHashKeyRule) { bean ->
        bean.autowire = 'byName'
    }

//Spectra curation workflow
    lcmsSpectraIdentification(LCMSSpectraIdentificationRule) { bean ->
        bean.autowire = 'byName'
    }

    gcmsSpectraIdentification(GCMSSpectraIdentificationRule) { bean ->
        bean.autowire = 'byName'
    }

//limit our collision energy in case of percentages to under 100 and over 0
    collisionEnergyPercentageRule(PercentageValueRule, "collision energy") { bean ->
        bean.autowire = 'byName'
        minPercentage = 0
        maxPercentage = 100
    }

//flow gradiant percentage rule
    flowGradientPercentageRule(PercentageValueRule, "flow gradient") { bean ->
        bean.autowire = 'byName'
        minPercentage = 0
        maxPercentage = 100
    }

//solvent percentage rule
    solventPercentageRule(PercentageValueRule, "solvent") { bean ->
        bean.autowire = 'byName'
        minPercentage = 0
        maxPercentage = 100
    }

//tests the precision of the ions in a mass spec
    preciseEnough(MassSpecIsPreciseEnoughRule) { bean ->
        bean.autowire = 'byName'
        minPrecision = 3
    }

//does the spectra has any annotations
    isAnnotatedSpectraRule(IsAnnotatedSpectraRule) { bean ->
        bean.autowire = 'byName'
    }

//spectra should always be relative and not absolute
    convertSpectraToRelativeRule(ConvertMassspectraToRelativeSpectraRule) { bean ->
        bean.autowire = 'byName'
    }

//is spectra dirty
    isSpectraDirty(IsCleanSpectraRule) { bean ->
        bean.autowire = 'byName'
        noisePercentage = 2
        percentOfSpectraIsNoise = 80
    }

    isSpectraDuplicated(IsDuplicatedSpectraRule) { bean ->
        bean.autowire = 'byName'
        minSimilarity = 900
    }

//is column metadata valid
    isColumnValid(IsColumnValid) { bean ->
        bean.autowire = 'byName'
    }

//verify that a lcms spectrum has valid adducts
    lcmsAdductCuration(LCMSAdductCurationRule) { bean ->
        bean.autowire = 'byName'
        toleranceInDalton = 0.5
        minAdducts = 1
    }

//verify that a lcms spectrum has valid adducts
    gcmsAdductCuration(GCMSAdductCurationRule) { bean ->
        bean.autowire = 'byName'
        toleranceInDalton = 1
        minAdducts = 1
    }

//GCMS Derivatization rules
    gcmsDerivatizationRule(ConfirmGCMSDerivatizationRule) { bean ->
        bean.autowire = 'byName'
    }

    gcmsPredictDerivatizedCompoundRule(PredictGCMSCompoundRule) { bean ->
        bean.autowire = 'byName'
    }

    gcmsValidateChemicalCompound(GCMSDerivatizationDoesntMatchCompound) { bean ->
        predictGCMSCompoundRule = gcmsPredictDerivatizedCompoundRule
        bean.autowire = 'byName'

    }

    gcmsPredictMMinus15Rule(PredictedMMinus15Rule) { bean ->
        bean.autowire = 'byName'
        predictGCMSCompoundRule = gcmsPredictDerivatizedCompoundRule

    }

    gcmsCompoundShouldBeDerivatized(CompoundShouldBeDerivatizedRule) { bean ->
        bean.autowire = 'byName'
        predictGCMSCompoundRule = gcmsPredictDerivatizedCompoundRule
        maximumNoneDerivatizedMass = 600

    }

//checks if the provided accurate mass is actuall possible
    exactMassIsPossibleRule(ExactMassIsCorrectRule) { bean ->
        bean.autowire = 'byName'
    }

//set up subcuration workflow to check if it's an accurate mass spectra
    isAccurateMassSpectra(SubCurationWorkflow, true) { bean ->
        bean.autowire = 'byName'
        rules = [
                preciseEnough
        ]

//   successAction = new AddTagAction("accurate")
//   failureAction = new RemoveTagAction("accurate")

    }



    derivativeTypeSpellingRule(DerivativeTypeSpelling) { bean ->
        bean.autowire = 'byName'
    }

    lipidBlastAquisitoinModeDetectionModeRule(LipidBlastAquisitionModeDetectionRule) { bean ->
        bean.autowire = 'byName'
    }

    lipidBlastMSMSDectionRule(LipidBlastMSMSDetectionRule) { bean ->
        bean.autowire = 'byName'
    }

    requiresRemoval(RemoveIdenticalSpectraRule) { bean ->
        bean.autowire = 'byName'
    }

    calculateMassAccuracyRule(CalculateMassAccuracyRule) { bean ->
        bean.autowire = 'byName'
    }

//set up metadata subcuration workflow
    metadataCuration(SubCurationWorkflow, "suspect values", false, "metadata curation") { bean ->
        bean.autowire = 'byName'
        rules = [
                lipidBlastAquisitoinModeDetectionModeRule,
                lipidBlastMSMSDectionRule,
                collisionEnergyPercentageRule,
                solventPercentageRule,
                flowGradientPercentageRule,
                isColumnValid,
                exactMassIsPossibleRule,
                derivativeTypeSpellingRule,
                gcmsDerivatizationRule

        ]
    }

//removes metadata we dont need
    dropNoneWantedMetaDataRule(DropNoneNecessaryMetaDataRule) { bean ->
        bean.autowire = 'byName'

        dataToDrop = [
                //all these data reflect compound, so no need in spectra objects to store them twice!
                "smiles",
                "inchi key",
                "inchi",
                //spectra propertie, which is not really needed
                "num peaks",
                "inchiaux"
        ]
    }

    generateFragmentationTreesRuleForMassBank(GenerateFragmentationTreesRuleForMassBank) { bean ->
        bean.autowire = 'byName'
    }

//define our complete workflow here
    spectraCurationWorkflow(CurationWorkflow) { bean ->
        bean.autowire = 'byName'

        rules = [
                /*
                //these rules should run first
                deleteRuleBasedTagRule,
                deleteMetaDataRule,
                generateHashKeyRule,
                dropNoneWantedMetaDataRule,
                remapMetadataNames,
                removeTinyIonRule,
                lcmsSpectraIdentification,
                gcmsSpectraIdentification,
                calculateMassAccuracyRule,

                //order doesn't really matter here
                metadataCuration,
                //isSpectraDuplicated,
                isAccurateMassSpectra,
                isSpectraDirty,
                lcmsAdductCuration,
                gcmsAdductCuration,
                gcmsPredictDerivatizedCompoundRule,
                gcmsValidateChemicalCompound,
                gcmsPredictMMinus15Rule,
                gcmsCompoundShouldBeDerivatized,

                //these rules should run last
                isAnnotatedSpectraRule,

                //fragmentation tree generation
                generateFragmentationTreesRuleForMassBank,

*/
                //must be the last rule
                requiresRemoval

        ]
//define and register our curation
    }
}