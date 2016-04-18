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

        /**
         * parses a query object and returns a RSQL Query String
         * @param query
         * @return query
         */
        function parseRSQL(query) {
            var queryString = "";

            //build compound query
            if (Object.keys(query.compound).length !== 0 && JSON.stringify(query.compound) !== JSON.stringify({})) {
                queryString += buildCompoundQueryString(query.compound);
            }

            // build metadata query
            if (Object.keys(query.metadata.length !== 0)) {

            }
            $log.info(query);
            $log.info(queryString);

            // for each keys in
            // compound
            // metadata
            // tags

            // concat key value to query string


        }

        function buildCompoundQueryString(compoundQuery) {
            var bio = "";
            var chem = "";

            // handle compound name
            if(typeof(compoundQuery.name) !== 'undefined') {
                bio += "biologicalCompound.names=q='name==" + '\"' + compoundQuery.name + '\"\'';
                chem += "chemicalCompound.names=q='name==" + '\"' + compoundQuery.name + '\"\'';

            }

            // handle compound inchiKey
            if(typeof(compoundQuery.inchiKey) !== 'undefined') {
                bio += " or biologicalCompound=q=inchiKey==" + '\"' + compoundQuery.inchiKey + '\"\'';
                chem += " or chemicalCompound=q=inchiKey==" + '\"' + compoundQuery.inchiKey + '\"\'';
            }
            console.log( bio + ' or ' + chem);
            return bio + ' or ' + chem;
        }

    }
})();