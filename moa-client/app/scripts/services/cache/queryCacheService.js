/**
 * Created by sajjan on 8/21/14.
 *
 * Stores the current query for persistence and updating between views
 */

(function() {
    'use strict';
    angular.module('moaClientApp')
      .service('QueryCache', queryCache);

    /* @ngInject */
    function queryCache($injector, $log, $rootScope) {
        /**
         * Stored query
         */
        this.query = null;

        /**
         * Clear all values stored in this cache factory
         */
        this.clear = function() {
            this.query = null;
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
        this.getSpectraQuery = function() {
            // Create default query if none exists
            // Using $injector is ugly, but is what angular.run uses to avoid circular dependency

            if (this.query === null) {
                return $injector.get('SpectraQueryBuilderService').prepareQuery();
            } else {
                return this.query;
            }
        };

        /**
         * sets a new spectra query
         * @param query
         */
        this.setSpectraQuery = function(query) {
            $rootScope.$broadcast('spectra:query', query);
            parseRSQL(query);
            this.query = query;
        };

        /**
         * Resets the current query
         */
        this.resetSpectraQuery = function() {
            this.clear();
        };
        
    }
})();
