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
            return typeof cache.get('viewSpectrum') !== 'undefined';
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
            return typeof cache.get('spectra') !== 'undefined';
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
            return typeof cache.get('query') !== 'undefined';
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


        getTags: function(callback) {
            if(!cache.get('tags')) {
                cache.put('tags', TaggingService.query(
                    callback,
                    function (error) {
                        alert('failed: ' + error);
                    }
                ));
            } else {
                callback(cache.get('tags'));
            }
        },

        getMetadataCategories: function(callback) {
            if(!cache.get('metadataCategories')) {
                cache.put('metadataCategories', MetadataService.categories(
                    callback,
                    function (error) {
                        $log.error('metadata categories failed: '+ error);
                    }
                ));
            } else {
                callback(cache.get('metadataCategories'));
            }
        },

        getMetadata: function(callback) {
            if (!cache.get('metadata')) {
                cache.put('metadata', MetadataService.metadata(
                    callback,
                    function (error) {
                        $log.error('metadata failed: ' + error);
                    }
                ));
            } else {
                callback(cache.get('metadata'));
            }
        }
    };
});

app.factory('ScrollCache', function($cacheFactory) {
    return $cacheFactory('scroll');
});