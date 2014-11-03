/**
 * Created by sajjan on 8/21/14.
 */


/**
 * Stores spectra browser data, individual spectrum, and query data for
 * persistence between controllers and views
 */
app.factory('SpectrumCache', function($cacheFactory) {
    var cache = $cacheFactory('spectrum');

    return {
        /**
         * Retrieve this cache factory
         */
        cache: cache,

        /**
         * Clear all values stored in this cache factory
         */
        clear: function () {
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
        }
    };
});


/**
 * Stores the current query for persistence and updating between views
 */
app.factory('QueryCache', function($cacheFactory, $injector) {
    var cache = $cacheFactory('query');

    return {
        /**
         * Retrieve this cache factory
         */
        cache: cache,

        /**
         * Clear all values stored in this cache factory
         */
        clear: function () {
            cache.removeAll();
        },


        /**
         * Retruns whether a query exists
         * @returns {boolean}
         */
        hasSpectraQuery: function() {
            return typeof cache.get('spectraQuery') !== 'undefined';
        },

        /**
         * returns our query or creates a default query if it does not exist
         * @returns {*|QueryCache.spectraQuery}
         */
        getSpectraQuery: function () {
            // Create default query if none exists
            // Using $injector is ugly, but is what angular.run uses to avoid circular dependency
            if(typeof cache.get('spectraQuery') === 'undefined') {
                this.setSpectraQuery($injector.get('SpectraQueryBuilderService').prepareQuery());
            }

            return cache.get('spectraQuery');
        },

        /**
         * sets a new spectra query
         * @param query
         */
        setSpectraQuery: function (query) {
            cache.put('spectraQuery', query);
        },

        /**
         * Resets the current query
         */
        resetSpectraQuery: function () {
            cache.remove('spectraQuery');
        }
    }
});


/**
 * Stores commonly used data obtained from the server to reduce load time
 * of certain views
 */
app.factory('AppCache', function($cacheFactory, MetadataService, TaggingService, INTERNAL_CACHING) {
    var cache = $cacheFactory('app');

    return {
        /**
         * Retrieve this cache factory
         */
        cache: cache,

        /**
         * Clear all values stored in this cache factory
         */
        clear: function () {
            cache.removeAll();
        },


        getTags: function(callback) {
            if(!INTERNAL_CACHING || !cache.get('tags')) {
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
            if(!INTERNAL_CACHING || !cache.get('metadataCategories')) {
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
            if (!INTERNAL_CACHING || !cache.get('metadata')) {
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


/**
 * In progress
 */
app.factory('ScrollCache', function($cacheFactory) {
    return $cacheFactory('scroll');
});