import auth.SubmitterRestAuthenticationTokenJsonRenderer
import curation.CurationObject
import curation.CurationWorkflow
import curation.SubCurationWorkflow
import curation.actions.AddTagAction
import curation.actions.RemoveTagAction
import curation.rules.adduct.gcms.CompoundShouldBeDerivatizedRule
import curation.rules.adduct.gcms.ConfirmGCMSDerivatizationRule
import curation.rules.adduct.gcms.GCMSAdductCurationRule
import curation.rules.adduct.lcms.LCMSAdductCurationRule
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
import curation.rules.meta.lipidblast.LipidBlastMSMSDetectionRule
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

    /**
     * workflow for compound curation is defined here
     */
    loadBeans('classpath*:compoundCuration.groovy')

    /**
     * workflow for spectra curation is definied here
     */
    loadBeans('classpath*:spectraCuration.groovy')

    /**
     * scoring of the system
     */
    loadBeans('classpath*:spectraScoring.groovy')


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
