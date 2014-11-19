/**
 * Created by sajjan on 8/21/14.
 */


/**
 * Stores spectra browser data, individual spectrum, and query data for
 * persistence between controllers and views
 */
app.service('SpectrumCache', function ($log) {
    /**
     * Stored browser spectra
     */
    this.spectra = null;

    /**
     * Stored spectrum for viewing
     */
    this.spectrum = null;


    /**
     * Clear all values stored in this cache factory
     */
    this.clear = function () {
        this.removeBrowserSpectra();
        this.removeSpectrum();
    };


    this.hasBrowserSpectra = function () {
        return this.spectra != null;
    };
    this.getBrowserSpectra = function () {
        return this.spectra;
    };
    this.setBrowserSpectra = function (spectra) {
        this.spectra = spectra;
    };
    this.removeBrowserSpectra = function () {
        this.spectra = null;
    };


    this.hasSpectrum = function () {
        return this.spectrum != null;
    };
    this.getSpectrum = function () {
        return this.spectrum;
    };
    this.setSpectrum = function (spectrum) {
        this.spectrum = spectrum;
    };
    this.removeSpectrum = function () {
        this.spectrum = null;
    };
});


/**
 * Stores the current query for persistence and updating between views
 */
app.service('QueryCache', function ($injector, $log, $rootScope) {
    /**
     * Stored query
     */
    this.query = null;

    /**
     * Clear all values stored in this cache factory
     */
    this.clear = function () {
        this.query = null;
    };


    /**
     * Returns whether a query exists
     * @returns {boolean}
     */
    this.hasSpectraQuery = function () {
        return this.query != null;
    };

    /**
     * returns our query or creates a default query if it does not exist
     * @returns {*|query}
     */
    this.getSpectraQuery = function () {
        // Create default query if none exists
        // Using $injector is ugly, but is what angular.run uses to avoid circular dependency
        if (this.query == null) {
            return $injector.get('SpectraQueryBuilderService').prepareQuery();
        }

        else {
            return this.query;
        }
    };

    /**
     * sets a new spectra query
     * @param query
     */
    this.setSpectraQuery = function (query) {
        $rootScope.$broadcast('spectra:query', query);
        this.query = query;
    };

    /**
     * Resets the current query
     */
    this.resetSpectraQuery = function () {
        this.clear();
    }
});


/**
 * Stores commonly used data obtained from the server to reduce load time
 * of certain views
 */
app.service('AppCache', function (MetadataService, TaggingService, INTERNAL_CACHING, $log) {
    /**
     * Stored tags
     */
    this.tags = null;

    /**
     * Stored metadata
     */
    this.metadata = null;

    /**
     * Stored metadata categories
     */
    this.metadataCategories = null;


    /**
     * Clear all values stored in this cache factory
     */
    this.clear = function () {
        this.tags = null;
        this.metadata = null;
        this.metadataCategories = null;
    };


    /**
     * Retrieve tags either from the rest api or internal cache
     */
    this.getTags = function (callback) {
        if (!INTERNAL_CACHING || this.tags == null) {
            this.tags = TaggingService.query(
                callback,
                function (error) {
                    $log.error('failed: ' + error);
                }
            );
        } else {
            callback(this.tags);
        }
    };


    /**
     * Retrieve tags statistics either from the rest api or internal cache
     */
    this.getTagsStatistics = function (callback) {
        if (!INTERNAL_CACHING || this.tagsStatistics == null) {
            this.tagsStatistics = TaggingService.statistics(
                callback,
                function (error) {
                    $log.error('failed: ' + error);
                }
            );
        } else {
            callback(this.tagsStatistics);
        }
    };

    /**
     * Retrieve metadata fields either from the rest api or internal cache
     */
    this.getMetadata = function (callback) {
        if (!INTERNAL_CACHING || this.metadata == null) {
            this.metadata = MetadataService.metadata(
                callback,
                function (error) {
                    $log.error('metadata failed: ' + error);
                }
            );
        } else {
            callback(this.metadata);
        }
    };

    /**
     * Retrieve metadata categories either from the rest api or internal cache
     */
    this.getMetadataCategories = function (callback) {
        if (!INTERNAL_CACHING || this.metadataCategories == null) {
            this.metadataCategories = MetadataService.categories(
                callback,
                function (error) {
                    $log.error('metadata categories failed: ' + error);
                }
            );
        } else {
            callback(this.metadataCategories);
        }
    };
});


/**
 * In progress
 */
app.factory('ScrollCache', function ($cacheFactory) {
    return $cacheFactory('scroll');
});