/**
 * Created by wohlgemuth on 7/10/14.
 *
 * a service to build our specific query object to be executed against the Spectrum service, mostly required for the modal query dialog and so kinda special
 */

(function() {
    'use strict';
    SpectraQueryBuilderService.$inject = ['$log', '$location', '$route'];
    angular.module('moaClientApp')
        .service('SpectraQueryBuilderService', SpectraQueryBuilderService);

    /* @ngInject */
    function SpectraQueryBuilderService($log, $location, $route) {

        /**
         * Stored query
         */
        this.query = [];

        /**
         * Stored RSQL query string, only used when the query is set from outside this query builder
         */
        this.queryString = '';

        /**
         * Stored text search
         */
        this.textSearch = '';


        this.getQuery = function() {
            if (this.query == null) {
                this.prepareQuery()
            }

            return this.query;
        };

        this.setQuery = function(query) {
            this.query = query;
            this.queryString = '';
        };

        this.setQueryString = function(queryString) {
            this.query = [];
            this.queryString = queryString;
        };

        this.getTextSearch = function() {
            return this.textSearch;
        };

        this.setTextSearch = function(textSearch) {
            this.textSearch = textSearch;
        };

        this.prepareQuery = function() {
            $log.debug('Resetting query');

            this.query = [];
            this.queryString = '';
            this.textSearch = '';
        };

        /**
         * Generate RSQL query from the query components.  Uses queryString as a base
         * if provided so that a user can start with a predefined or user-specified query
         * and add additional search terms to it.
         */
        this.getRSQLQuery = function() {
            if (this.queryString == '') {
                return this.query.join(' and ');
            } else {
                return this.query.concat([this.queryString]).join(' and ');
            }
        };

        this.executeQuery = function(replace) {
            var query = this.getRSQLQuery();

            if (query !== '' || this.textSearch !== '') {
                $log.info('Executing RSQL query: "'+ query + '", and text search: "'+ this.textSearch +'"');
                $location.path('/spectra/browse').search({query: query, text: this.textSearch});
            } else {
                if ($location.path() === '/spectra/browse' && angular.equals($location.search(), {})) {
                    $log.debug('Reloading route');
                    $route.reload();
                } else {
                    $log.debug('Executing empty query');
                    $location.path('/spectra/browse').search({});
                }
            }

            replace = typeof replace !== 'undefined' ? replace : false;

            if (replace) {
                $location.replace();
            }
        };


        /**
         * Stored similarity query
         */
        this.similarityQuery = null;

        this.setSimilarityQuery = function(query) {
            this.similarityQuery = query;
        };

        this.hasSimilarityQuery = function() {
            return this.similarityQuery != null;
        };

        this.getSimilarityQuery = function() {
            return this.similarityQuery;
        };


        /**
         * Build a metadata query, using a recursive approach if dealing with an array of values
         * @param name name of metadata field
         * @param value value(s) to query by
         * @param collection metadata field to query within (e.g. metaData, compound.metaData, compound.classification)
         * @param tolerance tolerance value for floating-point queries
         * @param partialQuery whether to perform a partial string search
         * @returns {string}
         */
        var buildMetaDataQuery = function(name, value, collection, tolerance, partialQuery) {
            // Handle array of values
            if (Array.isArray(value)) {
                var subqueries = value.map(function(x) {
                    return buildMetaDataQuery(name, x, collection, tolerance, partialQuery);
                });

                return '('+ subqueries.join(' or ') + ')';
            }

            // Handle individual values
            else {
                if (typeof tolerance !== 'undefined') {
                    var leftBoundary = parseFloat(value) - tolerance;
                    var rightBoundary = parseFloat(value) + tolerance;

                    return collection + '=q=\'name=="' + name + '" and value >= '+ leftBoundary +' and value <= '+ rightBoundary +'\''
                } else if (typeof partialQuery !== 'undefined') {
                    return collection + '=q=\'name=="' + name + '" and value=match=".*' + value + '.*"\'';
                } else {
                    return collection + '=q=\'name=="' + name + '" and value=="' + value + '"\'';
                }
            }
        };

        this.addMetaDataToQuery = function(name, value, partialQuery) {
            this.query.push(buildMetaDataQuery(name, value, 'metaData', undefined, partialQuery));
        };

        this.addNumericalMetaDataToQuery = function(name, value, tolerance) {
            this.query.push(buildMetaDataQuery(name, value, 'metaData', tolerance, undefined));
        };

        this.addCompoundMetaDataToQuery = function(name, value, partialQuery) {
            this.query.push(buildMetaDataQuery(name, value, 'compound.metaData', undefined, partialQuery));
        };

        this.addNumericalCompoundMetaDataToQuery = function(name, value, tolerance) {
            this.query.push(buildMetaDataQuery(name, value, 'compound.metaData', tolerance, undefined));
        };

        this.addClassificationToQuery = function(name, value, partialQuery) {
            this.query.push(buildMetaDataQuery(name, value, 'compound.classification', undefined, partialQuery));
        };

        this.addGeneralClassificationToQuery = function(value) {
            this.query.push('compound.classification=q=\'value=match=".*'+ value +'.*"\'');
        };

        this.addNameToQuery = function(name) {
            this.query.push('compound.names=q=\'name=like="'+ name +'"\'');
        };

        var buildTagQuery = function(value, collection, queryType) {
            // Handle array of values
            if (Array.isArray(value)) {
                var subqueries = value.map(function(x) {
                    return buildTagQuery(x, collection, queryType);
                });

                return '('+ subqueries.join(' or ') + ')';
            }

            // Handle individual values
            else {
                if (typeof queryType !== 'undefined' && queryType == 'match') {
                    return collection + '.text=match=".*'+ value +'.*"';
                } else if (typeof queryType !== 'undefined' && queryType == 'ne') {
                    return collection +'.text!="'+ value +'"';
                } else {
                    return collection +'.text=="'+ value +'"';
                }
            }
        };

        this.addTagToQuery = function(query, queryType) {
            this.query.push(buildTagQuery(query, 'tags', queryType));
        };

        this.addCompoundTagToQuery = function(query, queryType) {
            this.query.push(buildTagQuery(query, 'compound.tags', queryType));
        };

        this.addSplashToQuery = function(query) {
            if (/^(splash[0-9]{2}-[a-z0-9]{4}-[0-9]{10}-[a-z0-9]{20})$/.test(query)) {
                this.query.push('splash.splash=="'+ query +'"');
            } else if (/^splash[0-9]{2}/.test(query)) {
                this.query.push('splash.splash=match="'+ query +'.*"');
            } else {
                this.query.push('splash.splash=match=".*'+ query +'.*"');
            }
        };

        this.addUserToQuery = function(username) {
            this.query.push('submitter.id=="'+ username +'"');
        };
    }
})();
