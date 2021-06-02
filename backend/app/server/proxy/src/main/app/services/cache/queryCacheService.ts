/**
 * Created by sajjan on 8/21/14.
 *
 * Stores the current query for persistence and updating between views
 */

import * as angular from 'angular';

class QueryCacheService{
    private static $inject = ['$injector', '$log', '$rootScope'];
    private $injector;
    private $log;
    private $rootScope;
    private query;
    private queryString;

    constructor($injector, $log, $rootScope) {
        this.$injector = $injector;
        this.$log = $log;
        this.$rootScope = $rootScope;
    }

    $onInit = () => {
        /**
         * Stored query
         */
        this.query = null;

        /**
         * Stored rsqlQueryString
         */
        this.queryString = null;
    }

    /**
     * Clear all values stored in this cache factory
     */
    clear = () => {
        this.query = null;
        this.queryString = null;
    };

    /**
     * Returns whether a query exists
     * @returns {boolean}
     */
    hasSpectraQuery = () => {
        return this.query !== null;
    };

    /**
     * returns our query or creates a default query if it does not exist
     * @returns {*|query}
     */
    getSpectraQuery = (queryType) => {
        // Create default query if none exists
        // Using $injector is ugly, but is what angular.run uses to avoid circular dependency
        queryType = queryType || undefined;

        if (this.query === null && this.queryString === null) {
            this.$injector.get('SpectraQueryBuilderService').prepareQuery();
        }

        return typeof(queryType) !== 'undefined' && queryType === 'string' ? this.queryString : this.query;
    };

    /**
     * sets a new spectra query
     * @param query
     */
    setSpectraQuery = (query) => {
        this.$rootScope.$broadcast('spectra:query', query);

        this.query = query;
    };

    setSpectraQueryString = (queryString) => {
        this.queryString = queryString;
    };

    /**
     * Resets the current query
     */
    resetSpectraQuery = () => {
        this.clear();
    };
}

angular.module('moaClientApp')
    .service('QueryCache', QueryCacheService);
