/**
 * This factory will parse the query Modal object and returns an RSQL Query String
 *
 */

(function () {
    'use strict';
    queryStringBuilder.$inject = ['$log', 'QueryCache', 'qStrHelper'];
    angular.module('moaClientApp')
        .factory("queryStringBuilder", queryStringBuilder);

    /* @ngInject */
    function queryStringBuilder($log, QueryCache, qStrHelper) {
        var defaultQuery = '/rest/spectra';
        var queryStr;
        var operand;
        var compiled;

        var service = {
            buildQuery: buildQuery,
            buildAdvanceQuery: buildAdvanceQuery,
            updateQuery: updateQuery

        };
        return service;


        /**
         * updates on the fly queries submitted by users
         */
        function updateQuery() {
            // options: compound, metadata, tags
            var query = QueryCache.getSpectraQuery();
	        compiled = [];

	        // updates compound metadata
	        if (angular.isDefined(query.compound.metadata) && query.compound.metadata.length > 0) {
		        queryStr = qStrHelper.buildMetaString(query.compound.metadata, true);
		        compiled.push(queryStr);
	        }

	        // updates metadata
	        if (angular.isDefined(query.metadata) && query.metadata.length > 0) {
		        queryStr = qStrHelper.buildMetaString(query.metadata);
		        compiled.push('and', queryStr);
	        }
	        saveQuery();

        }

        /**
         * builds a queryString when user submit keywordFilter form
         * @return rsql query string
         */
        function buildQuery() {
            var query = QueryCache.getSpectraQuery();
            compiled = [];

            validateOperands(query);

            console.log(query)

            // build compound string
            if (angular.isDefined(query.compound) && query.compound.length > 0) {
                compiled.push(qStrHelper.buildCompoundString(query.compound));
            }

            // build compound metadata string
            operand = query.operand.shift();
            if (angular.isDefined(query.compoundDa) && query.compoundDa.length > 0) {
                queryStr = qStrHelper.buildMeasurementString(query.compoundDa);
                compiled.push(operand, queryStr);
            }

            // add formula
            operand = query.operand.shift();
            if (angular.isDefined(query.formula)) {
                queryStr = "compound.metaData=q='name==\"molecular formula\" and value=match=\".*" + query.formula + ".*\"'";
                compiled.push(operand, queryStr);
            }

            // add classification
            operand = query.operand.shift();
            if (angular.isDefined(query.classification)) {
                queryStr = "compound.classification=q='value=match=\".*" + query.classification + ".*\"'";
                compiled.push(operand, queryStr);
            }

            //build metadata filter string from search page
            if (angular.isDefined(query.groupMeta)) {
                queryStr = qStrHelper.addMetaFilterQueryString(query.groupMeta);
                if (queryStr !== '') {
                    compiled.push('and', queryStr);
                }
            }

            saveQuery();
        }


        /**
         * builds a queryString when user submit advancedSearch form
         * @return rsql query string
         */
        function buildAdvanceQuery() {
            var query = QueryCache.getSpectraQuery();
            compiled = [];

            // compound name, inchiKey and class
            operand = query.operand.compound.shift();
            if (angular.isDefined(query.compound) && query.compound.length > 0) {
                queryStr = qStrHelper.buildCompoundString(query.compound);

                // add user's selected operand
                var re = /or compound.classification/;
                var newStr = operand.concat(' compound.classification');
                queryStr = queryStr.replace(re, newStr);

                compiled.push(queryStr);
            }

            // add compound metadata
            operand = query.operand.compound.shift();
            if (angular.isDefined(query.compound.metadata) && query.compound.metadata.length > 0) {
                queryStr = qStrHelper.buildMetaString(query.compound.metadata, true);
                compiled.push(operand, queryStr);
            }

            operand = query.operand.compound.shift();
            if (angular.isDefined(query.compoundDa) && query.compoundDa.length > 0) {
                queryStr = qStrHelper.buildMeasurementString(query.compoundDa);
                compiled.push(operand, queryStr);
            }

            // add metadata query
            if (angular.isDefined(query.metadata) && query.metadata.length > 0) {
                queryStr = qStrHelper.buildMetaString(query.metadata);
                compiled.push('and', queryStr);
            }

            // add metadata measurement
            operand = query.operand.metadata.pop();
            if (angular.isDefined(query.metadataDa) && query.metadataDa.length > 0) {

            }


            saveQuery();
        }

        function saveQuery() {
            // remove any leading operators
            if (compiled[0] === 'and' || compiled[0] === 'or') {
                compiled.shift();
            }

            compiled = compiled.length > 0 ? compiled.join(' ') : defaultQuery;
            QueryCache.setSpectraQueryString(compiled);
        }

        function validateOperands(query) {
            if(!angular.isDefined(query.operand)) {
                throw new TypeError('query.operand property is undefined');
            }
        }

        function buildTagsQueryString(tagQuery) {
            var queryString = "";
            for (var i = 0, l = tagQuery.length; i < l; i++) {
                if (i > 0) {
                    queryString += ' and ';
                }
                queryString += "tags=q='name.eq==" + tagQuery[i].name.eq + '\"\'';

            }
            return queryString;
        }


    }
})();
