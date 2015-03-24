import auth.SubmitterRestAuthenticationTokenJsonRenderer
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
import curation.rules.meta.DerivativeTypeSpelling
import curation.rules.meta.DropNoneNecessaryMetaDataRule
import curation.rules.meta.IsColumnValid
import curation.rules.meta.PercentageValueRule
import curation.rules.meta.ProvidedExactMassIsPossibleRule
import curation.rules.meta.lipidblast.LipidBlastAquisitionModeDetectionRule
import curation.rules.spectra.ConvertMassspectraToRelativeSpectraRule
import curation.rules.spectra.IsAnnotatedSpectraRule
import curation.rules.spectra.IsCleanSpectraRule
import curation.rules.spectra.IsDuplicatedSpectraRule
import curation.rules.spectra.MassSpecIsPreciseEnoughRule
import curation.rules.tag.RemoveComputedTagRule
import persistence.metadata.filter.Filters
import persistence.metadata.filter.NameDoesntMatchFilter
import persistence.metadata.filter.NameMatchesFilter
import grails.spring.BeanBuilder
import persistence.metadata.filter.unit.BasicUnitConverter
import persistence.metadata.filter.unit.Converters
import util.caching.SpectrumKeyGenerator


// Place your Spring DSL code here
beans = {
    // Authentication beans
    restAuthenticationTokenJsonRenderer(SubmitterRestAuthenticationTokenJsonRenderer)

    //rest service generation for client side stuff
    rest(grails.plugins.rest.client.RestBuilder)

    //key generation for caching
    cacheKey(SpectrumKeyGenerator)

    //computes the compound validation data
    computeCompoundValidationData(CompoundComputeMetaDataRule) { bean ->
        bean.autowire = 'byName'
    }
    inchiKeyMatchesMolFile(VerifyInChIKeyAndMolFileMatchRule) { bean ->
        bean.autowire = 'byName'
    }

    deleteMetaDataRule(DeletedComputedMetaDataRule) { bean ->
        bean.autowire = 'byName'
    }


    deleteRuleBasedTagRule(RemoveComputedTagRule) { bean ->
        bean.autowire = 'byName'
    }



    compoundCurationWorkflow(CurationWorkflow) { bean ->
        bean.autowire = 'byName'

        rules = [
                deleteMetaDataRule,
                deleteRuleBasedTagRule,
                computeCompoundValidationData,
                inchiKeyMatchesMolFile
        ]
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
    exactMassIsPossibleRule(ProvidedExactMassIsPossibleRule) { bean ->
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

//set up metadata subcuration workflow
    metadataCuration(SubCurationWorkflow, "suspect values", false, "metadata curation") { bean ->
        bean.autowire = 'byName'
        rules = [
                lipidBlastAquisitoinModeDetectionModeRule,
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
                "num peaks"
        ]
    }

//define our complete workflow here
    spectraCurationWorkflow(CurationWorkflow) { bean ->
        bean.autowire = 'byName'

        rules = [
                //these rules should run first
                deleteRuleBasedTagRule,
                deleteMetaDataRule,
                dropNoneWantedMetaDataRule,
                lcmsSpectraIdentification,
                gcmsSpectraIdentification,

                //order doesn't really matter here
                metadataCuration,
                isSpectraDuplicated,
                isAccurateMassSpectra,
                isSpectraDirty,
                lcmsAdductCuration,
                gcmsAdductCuration,
                gcmsPredictDerivatizedCompoundRule,
                gcmsValidateChemicalCompound,
                gcmsPredictMMinus15Rule,
                gcmsCompoundShouldBeDerivatized,

                //these rules should run last
                isAnnotatedSpectraRule
        ]
//define and register our curation
    }

//metadata filter, we only care for certain fields
    metadataFilters(Filters) { bean ->
        bean.autowire = 'byName'

        //saves us time deleting them later in the rules system
        filters = [
                new NameDoesntMatchFilter("SCIENTIFIC_NAME"),
                new NameDoesntMatchFilter("LINEAGE"),
                new NameDoesntMatchFilter("ACCESSION"),
                new NameDoesntMatchFilter("SAMPLE"),
                new NameDoesntMatchFilter("COMPOUND_CLASS"),
                new NameDoesntMatchFilter("taxonomy"),
                new NameDoesntMatchFilter("COMMENT"),
                new NameDoesntMatchFilter("pubchem"),
                new NameDoesntMatchFilter("chemspider"),
                new NameDoesntMatchFilter("cas"),
                new NameDoesntMatchFilter("kegg"),
                new NameDoesntMatchFilter("knapsack"),
                new NameDoesntMatchFilter("lipidbank"),
                new NameDoesntMatchFilter("date"),
                new NameDoesntMatchFilter("cayman"),
                new NameDoesntMatchFilter("chebi"),
                new NameDoesntMatchFilter("hmdb"),
                new NameDoesntMatchFilter("nikkaji"),
                new NameDoesntMatchFilter("chempdb"),
                new NameDoesntMatchFilter("inchikey"),
                new NameDoesntMatchFilter("inchi key"),
                new NameDoesntMatchFilter("casno"),
                new NameDoesntMatchFilter("mv"),
                new NameDoesntMatchFilter("comments"),
                new NameDoesntMatchFilter("kappaview"),
                new NameDoesntMatchFilter("lipidmaps"),
                new NameDoesntMatchFilter("internal standard"),
                new NameDoesntMatchFilter("name")
        ]
    }

//tries to discover units for us and converts them on the fly
    metadataValueConverter(Converters) { bean ->
        bean.autowire = 'byName'
        converters = [
                new BasicUnitConverter()
        ]
    }

}
