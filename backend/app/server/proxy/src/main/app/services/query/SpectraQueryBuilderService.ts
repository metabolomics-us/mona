/**
 * Created by wohlgemuth on 7/10/14.
 *
 * a service to build our specific query object to be executed against the Spectrum service, mostly required for the modal query dialog and so kinda special
 */

import * as angular from 'angular';

class SpectraQueryBuilderService {
    private static $inject = ['$log', '$location', '$route'];
    private $log;
    private $location;
    private $route;
    private query;
    private queryString;
    private textSearch;
    private similarityQuery;

    constructor($log, $location, $route) {
        this.$log = $log;
        this.$location = $location;
        this.$route = $route;
    }

    $onInit = () => {
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

        /**
         * Stored similarity query
         */
        this.similarityQuery = null;
    }

    getQuery = () => {
        if (this.query == null) {
            this.prepareQuery()
        }

        return this.query;
    };

    setQuery = (query) => {
        this.query = query;
        this.queryString = '';
    };

    setQueryString = (queryString) => {
        this.query = [];
        this.queryString = queryString;
    };

    getTextSearch = () => {
        return this.textSearch;
    };

    setTextSearch = (textSearch) => {
        this.textSearch = textSearch;
    };

    prepareQuery = () => {
        this.$log.debug('Resetting query');

        this.query = [];
        this.queryString = '';
        this.textSearch = '';
    };

    /**
     * Generate RSQL query from the query components.  Uses queryString as a base
     * if provided so that a user can start with a predefined or user-specified query
     * and add additional search terms to it.
     */
    getRSQLQuery = () => {
        if (this.queryString == '') {
            return this.query.join(' and ');
        } else {
            return this.query.concat([this.queryString]).join(' and ');
        }
    };

    executeQuery = (replace) => {
        let query = this.getRSQLQuery();

        if (query !== '' || this.textSearch !== '') {
            this.$log.info('Executing RSQL query: "'+ query + '", and text search: "'+ this.textSearch +'"');
            this.$location.path('/spectra/browse').search({query: query, text: this.textSearch});
        } else {
            if (this.$location.path() === '/spectra/browse' && angular.equals(this.$location.search(), {})) {
                this.$log.debug('Reloading route');
                this.$route.reload();
            } else {
                this.$log.debug('Executing empty query');
                this.$location.path('/spectra/browse').search({});
            }
        }

        replace = typeof replace !== 'undefined' ? replace : false;

        if (replace) {
            this.$location.replace();
        }
    };

    setSimilarityQuery = (query) => {
        this.similarityQuery = query;
    };

    hasSimilarityQuery = () => {
        return this.similarityQuery != null;
    };

    getSimilarityQuery = () => {
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
    buildMetaDataQuery =  (name, value, collection, tolerance, partialQuery) => {
        // Handle array of values
        if (Array.isArray(value)) {
            let subqueries = value.map((x) => {
                return this.buildMetaDataQuery(name, x, collection, tolerance, partialQuery);
            });

            return '('+ subqueries.join(' or ') + ')';
        }

        // Handle individual values
        else {
            if (typeof tolerance !== 'undefined') {
                let leftBoundary = parseFloat(value) - tolerance;
                let rightBoundary = parseFloat(value) + tolerance;

                return collection + '=q=\'name=="' + name + '" and value >= '+ leftBoundary +' and value <= '+ rightBoundary +'\''
            } else if (typeof partialQuery !== 'undefined') {
                return collection + '=q=\'name=="' + name + '" and value=match=".*' + value + '.*"\'';
            } else {
                return collection + '=q=\'name=="' + name + '" and value=="' + value + '"\'';
            }
        }
    };

    addMetaDataToQuery = (name, value, partialQuery) => {
        this.query.push(this.buildMetaDataQuery(name, value, 'metaData', undefined, partialQuery));
    };

    addNumericalMetaDataToQuery = (name, value, tolerance) => {
        this.query.push(this.buildMetaDataQuery(name, value, 'metaData', tolerance, undefined));
    };

    addCompoundMetaDataToQuery = (name, value, partialQuery) => {
        this.query.push(this.buildMetaDataQuery(name, value, 'compound.metaData', undefined, partialQuery));
    };

    addNumericalCompoundMetaDataToQuery = (name, value, tolerance) => {
        this.query.push(this.buildMetaDataQuery(name, value, 'compound.metaData', tolerance, undefined));
    };

    addClassificationToQuery = (name, value, partialQuery) => {
        this.query.push(this.buildMetaDataQuery(name, value, 'compound.classification', undefined, partialQuery));
    };

    addGeneralClassificationToQuery = (value) => {
        this.query.push('compound.classification=q=\'value=match=".*'+ value +'.*"\'');
    };

    addNameToQuery = (name) => {
        this.query.push('compound.names=q=\'name=like="'+ name +'"\'');
    };

    buildTagQuery = (value, collection, queryType) => {
        // Handle array of values
        if (Array.isArray(value)) {
            let subqueries = value.map((x) => {
                return this.buildTagQuery(x, collection, queryType);
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

    addTagToQuery = (query, queryType) => {
        this.query.push(this.buildTagQuery(query, 'tags', queryType));
    };

    addCompoundTagToQuery = (query, queryType) => {
        this.query.push(this.buildTagQuery(query, 'compound.tags', queryType));
    };

    addSplashToQuery = (query) => {
        if (/^(splash[0-9]{2}-[a-z0-9]{4}-[0-9]{10}-[a-z0-9]{20})$/.test(query)) {
            this.query.push('splash.splash=="'+ query +'"');
        } else if (/^splash[0-9]{2}/.test(query)) {
            this.query.push('splash.splash=match="'+ query +'.*"');
        } else {
            this.query.push('splash.splash=match=".*'+ query +'.*"');
        }
    };

    addUserToQuery = (username) => {
        this.query.push('submitter.id=="'+ username +'"');
    };


}

angular.module('moaClientApp')
    .service('SpectraQueryBuilderService', SpectraQueryBuilderService);
