import auth.SubmitterRestAuthenticationTokenJsonRenderer
import grails.plugins.rest.client.RestBuilder
import persistence.metadata.filter.Filters
import persistence.metadata.filter.NameDoesntMatchFilter
import persistence.metadata.filter.unit.BasicUnitConverter
import persistence.metadata.filter.unit.Converters
import util.caching.SpectrumKeyGenerator


// Place your Spring DSL code here
beans = {
    //authentication beans
    accessTokenJsonRenderer(SubmitterRestAuthenticationTokenJsonRenderer)

    //rest service generation for client side stuff
    rest(RestBuilder)

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


    /**
     * scoring of the system
     */
    loadBeans('classpath*:associateAccounts.groovy')


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
