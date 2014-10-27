/**
 * Created by sajjan on 8/21/14.
 */

app.factory('SpectrumCache', function($cacheFactory) {
    var cache = $cacheFactory('spectra');

    return {
        cache: cache,

        clear: function() {
            cache.removeAll();
        },


        hasSpectrum: function() {
            return typeof cache.get('viewSpectrum') != 'undefined';
        },
        getSpectrum: function() {
            return cache.get('viewSpectrum');
        },
        setSpectrum: function(spectrum) {
            cache.put('viewSpectrum', spectrum);
        },
        removeSpectrum: function() {
            cache.remove('viewSpectrum');
        },


        hasBrowserSpectra: function() {
            return typeof cache.get('spectra') != 'undefined';
        },
        getBrowserSpectra: function() {
            return cache.get('spectra');
        },
        setBrowserSpectra: function(spectrum) {
            cache.put('spectra', spectrum);
        },
        removeBrowserSpectra: function() {
            cache.remove('spectra');
        },


        hasQuery: function() {
            return typeof cache.get('query') != 'undefined';
        },
        getQuery: function() {
            return cache.get('query');
        },
        setQuery: function(query) {
            cache.put('query', query);
        },
        resetQuery: function() {
            cache.remove('query');
        }
    };
});

app.factory('AppCache', function($cacheFactory, MetadataService, TaggingService) {
    var cache = $cacheFactory('app');

    return {
        cache: cache,

        clear: function() {
            cache.removeAll();
        },

        getTags: function() {
            if(!cache.get('tags')) {
                cache.put('tags', TaggingService.query(
                    function (data) {},
                    function (error) {
                        alert('failed: ' + error);
                    }
                ));
            }

            return cache.get('tags');
        },

        getMetadataCategories: function() {
            if(!cache.get('metadataCategories')) {
                cache.put('metadataCategories', MetadataService.categories(
                    function (data) {},
                    function (error) {
                        $log.error('metadata categories failed: '+ error);
                    }
                ));
            }

            return cache.get('metaDataCategories');
        },

        getMetadata: function() {
            if(!cache.get('metaData')) {
                cache.put('metaData', MetadataService.metadata(
                    function (data) {},
                    function (error) {
                        $log.error('metadata failed: '+ error);
                    }
                ));
            }

            return cache.get('metaData');
        }
    };
});

app.factory('ScrollCache', function($cacheFactory) {
    return $cacheFactory('scroll');
});