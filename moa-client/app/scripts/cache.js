/**
 * Created by sajjan on 8/21/14.
 */


/**
 * Stores spectra browser data, individual spectrum, and query data for
 * persistence between controllers and views
 */
app.factory('SpectrumCache', function ($cacheFactory) {
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


        hasSpectrum: function () {
            return typeof cache.get('viewSpectrum') !== 'undefined';
        },
        getSpectrum: function () {
            return cache.get('viewSpectrum');
        },
        setSpectrum: function (spectrum) {
            cache.put('viewSpectrum', spectrum);
        },
        removeSpectrum: function () {
            cache.remove('viewSpectrum');
        },


        hasBrowserSpectra: function () {
            return typeof cache.get('spectra') !== 'undefined';
        },
        getBrowserSpectra: function () {
            return cache.get('spectra');
        },
        setBrowserSpectra: function (spectrum) {
            cache.put('spectra', spectrum);
        },
        removeBrowserSpectra: function () {
            cache.remove('spectra');
        }
    };
});


/**
 * Stores the current query for persistence and updating between views
 */
app.service('QueryCache', function ( $injector, $log, $rootScope) {


    /**
     * Retrieve this cache factory
     */
    this.query = null;

    /**
     * Clear all values stored in this cache factory
     */
    this.clear = function () {
        this.query = null;
    };


    /**
     * Retruns whether a query exists
     * @returns {boolean}
     */
    this.hasSpectraQuery = function () {
        return this.query != null;
    };

    /**
     * returns our query or creates a default query if it does not exist
     * @returns {*|QueryCache.spectraQuery}
     */
    this.getSpectraQuery = function () {
        // Create default query if none exists
        // Using $injector is ugly, but is what angular.run uses to avoid circular dependency
        if (this.query == null) {
            this.setSpectraQuery($injector.get('SpectraQueryBuilderService').prepareQuery());
        }

        return this.query;
    };

    /**
     * sets a new spectra query
     * @param query
     */
    this.setSpectraQuery = function (query) {
        $rootScope.$broadcast('spectra:query',query);
        this.query = query;
    };

    /**
     * Resets the current query
     */
    this.resetSpectraQuery = function () {
        this.clear();
    }

})
;


/**
 * Stores commonly used data obtained from the server to reduce load time
 * of certain views
 */
app.factory('AppCache', function ($cacheFactory, MetadataService, TaggingService, INTERNAL_CACHING) {
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


        getTags: function (callback) {
            if (!INTERNAL_CACHING || !cache.get('tags')) {
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

        getMetadataCategories: function (callback) {
            if (!INTERNAL_CACHING || !cache.get('metadataCategories')) {
                cache.put('metadataCategories', MetadataService.categories(
                    callback,
                    function (error) {
                        $log.error('metadata categories failed: ' + error);
                    }
                ));
            } else {
                callback(cache.get('metadataCategories'));
            }
        },

        getMetadata: function (callback) {
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
app.factory('ScrollCache', function ($cacheFactory) {
    return $cacheFactory('scroll');
});