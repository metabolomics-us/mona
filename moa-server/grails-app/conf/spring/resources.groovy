import util.caching.SpectrumKeyGenerator

// Place your Spring DSL code here
beans = {
    rest(grails.plugins.rest.client.RestBuilder)

    cacheKey(util.caching.SpectrumKeyGenerator)
}
