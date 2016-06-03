/**
 * This factory will parse the query Modal object and returns an RSQL Query String
 *
 */

(function () {
    'use strict';
    angular.module('moaClientApp')
        .factory("queryStringBuilder", queryStringBuilder);

    /* @ngInject */
    function queryStringBuilder($log, QueryCache, qStrHelper) {
        var defaultQuery = '/rest/spectra';

        var service = {
            buildQuery: buildQuery,
            buildAdvanceQuery: buildAdvanceQuery

        };
        return service;

        /**
         * builds a queryString when user submit keywordFilter form
         * @return rsql query string
         */
        function buildQuery() {
            var query = QueryCache.getSpectraQuery();
            var compiled = '';

            // build compound string
            if (angular.isDefined(query.compound) && query.compound.length !== 0) {
                compiled = qStrHelper.buildCompoundString(query.compound);
            }

            // build compound metadata string
            if (angular.isDefined(query.compoundDa) && query.compoundDa.length !== 0) {
                compiled = qStrHelper.buildMeasurementString(query.compoundDa, query.operand, compiled);
            }

            //build metadata filter string from search page
            if (angular.isDefined(query.groupMeta)) {
                var metadataQuery = addMetaFilterQueryString(query.groupMeta);

                compiled = compiled === '' && metadataQuery !== '' ? metadataQuery :
                    compiled !== '' && metadataQuery !== '' ? compiled.concat(' and ', metadataQuery) :
                        compiled;
            }

            compiled = compiled === '' ? defaultQuery : compiled;
            QueryCache.setSpectraQueryString(compiled);

        }

        // handles custom groupMeta for Keyword filter
        function addMetaFilterQueryString(filterOptions) {
            var filtered = [];
            for (var key in filterOptions) {
                if (filterOptions.hasOwnProperty(key) && filterOptions[key].length !== 0) {
                    filtered.push(addGroupMetaQueryString(key, filterOptions[key]));
                }
            }

            return filtered.length === 0 ? '' : filtered.length > 1 ? filtered.join(' and ') : filtered.join('');
        }

        // helper method for addMetaFilterQueryString
        function addGroupMetaQueryString(key, arr) {
            var query = [];

            for (var i = 0, l = arr.length; i < l; i++) {
                query.push("metaData=q='name==\"" + key + "\" and value==\"" + arr[i] + "\"'");
            }

            return '('.concat(query.length > 1 ? query.join(' or ') : query.join(''), ')');
        }




        /**
         * builds a queryString when user submit advancedSearch form
         * @return rsql query string
         */
        function buildAdvanceQuery() {
            var query = QueryCache.getSpectraQuery();
            var compiled = '';
            var operand = '';

            // compound name, inchiKey and class
            operand = query.operand.compound.shift();
            if (angular.isDefined(query.compound) && query.compound.length !== 0) {
                compiled = qStrHelper.buildCompoundString(query.compound);

                // add user's selected operand
                var re = /or compound.classification/;
                var newStr = operand.concat(' compound.classification');
                compiled = compiled.replace(re, newStr);
            }

            // add compound metadata
            operand = query.operand.compound.shift();
            if (angular.isDefined(query.compoundMetada && query.compoundMetada.length > 0)) {
                var compoundMeta = qStrHelper.buildMetaString(query.compoundMetada, true);

                compiled = compiled === '' && compoundMeta !== '' ? compoundMeta :
                    compiled !== '' && compoundMeta !== '' ? compiled.concat(' ', operand, ' ', compoundMeta) :
                        compiled;

            }

            if(angular.isDefined(query.compoundDa) && query.compoundDa.length > 0) {
                compiled = qStrHelper.buildMeasurementString(query.compoundDa, query.operand.compound, compiled);
                // empty last operand
                query.operand.compound.shift();
            }



            compiled = compiled === '' ? defaultQuery : compiled;
            QueryCache.setSpectraQueryString(compiled);
        }


        function addTagsQueryString(tagQuery) {
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
