/**
 * Created by sajjan on 8/21/14.
 *
 * Stores the current query for persistence and updating between views
 */

(function() {
    'use strict';
    queryCache.$inject = ['$injector', '$log', '$rootScope'];
    angular.module('moaClientApp')
      .service('QueryCache', queryCache);

    /* @ngInject */
    function queryCache($injector, $log, $rootScope) {
        /**
         * Stored query
         */
        this.query = null;

        /**
         * Stored rsqlQueryString
         */
        this.queryString = null;

        /**
         * Clear all values stored in this cache factory
         */
        this.clear = function() {
            this.query = null;
            this.queryString = null;
        };

        /**
         * Returns whether a query exists
         * @returns {boolean}
         */
        this.hasSpectraQuery = function() {
            return this.query !== null;
        };

        /**
         * returns our query or creates a default query if it does not exist
         * @returns {*|query}
         */
        this.getSpectraQuery = function(queryType) {
            // Create default query if none exists
            // Using $injector is ugly, but is what angular.run uses to avoid circular dependency
            queryType = queryType || undefined;

            if (this.query === null && this.queryString === null) {
                $injector.get('SpectraQueryBuilderService').prepareQuery();
            }

            return typeof(queryType) !== 'undefined' && queryType === 'string' ? this.queryString : this.query;
        };

        /**
         * sets a new spectra query
         * @param query
         */
        this.setSpectraQuery = function(query) {
            $rootScope.$broadcast('spectra:query', query);

            this.query = query;
        };

        this.setSpectraQueryString = function(queryString) {
            this.queryString = queryString;
        };

        /**
         * Resets the current query
         */
        this.resetSpectraQuery = function() {
            this.clear();
        };

    }
})();
