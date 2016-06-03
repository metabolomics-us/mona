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
                var compoundMetaQuery = addMeasurementQueryString(query.compoundDa, query.operand);

                // strip leading operators
                compiled = compiled === '' && compoundMetaQuery.substring(0,3) === 'and' ? compoundMetaQuery.slice(4) :
                    compiled === '' && compoundMetaQuery.substring(0,2) === 'or' ? compoundMetaQuery.slice(3) :
                        compiled.concat(' ',compoundMetaQuery);
            }

            //build metadata filter string from search page
            if(angular.isDefined(query.metaFilter)) {
               var metadataQuery = addMetaFilterQueryString(query.metaFilter);

                compiled = compiled === '' && metadataQuery !== '' ? metadataQuery :
                    compiled !== '' && metadataQuery !== '' ? compiled.concat(' and ', metadataQuery) :
                        compiled;
            }

            compiled = compiled === '' ? defaultQuery : compiled;
            QueryCache.setSpectraQueryString(compiled);

        }

        // handles custom metaFilter for Keyword filter
        function addMetaFilterQueryString(filterOptions) {
            var filtered = [];
            for (var key in filterOptions) {
                if(filterOptions.hasOwnProperty(key) && filterOptions[key].length !== 0) {
                    filtered.push(addGroupMetaQueryString(key, filterOptions[key]));
                }
            }

            return filtered.length === 0 ? '' : filtered.length > 1 ? filtered.join(' and ') : filtered.join('');
        }

        // helper method for addMetaFilterQueryString
        function addGroupMetaQueryString(key, arr) {
            var query = [];

            for(var i = 0, l = arr.length; i < l; i++) {
                query.push("metaData=q='name==\"" + key + "\" and value==\"" + arr[i] + "\"'");
            }

            return '('.concat(query.length > 1 ? query.join(' or ') : query.join(''),')');
        }


        // build query string for mass tolerance and formula
        function addMeasurementQueryString(measurement, operand) {
            var query = '';

            // handle exact mass & tolerance
            for (var i = 0; i < measurement.length; i ++) {

                if (measurement[i].hasOwnProperty('exact mass')) {
                    // concat first operand
                    query += operand[0];
                    var leftOffset = measurement[i]['exact mass'] - measurement[i+1].tolerance;
                    var rightOffset = measurement[i]['exact mass'] + measurement[i+1].tolerance;
                    query += " compound.metaData=q='name==\"exact mass\" and " + "value>=\"" + leftOffset + "\" or value<=\"" + rightOffset + "\"'";
                }

                // handle formula
                if (measurement[i].hasOwnProperty('formula')) {
                    query = query == '' ? query.concat(operand[1]) : query.concat(' ', operand[1]);
                    query +=" compound.metaData=q='name==\"formula\" and value==\"" + measurement[i].formula + "\"'";
                }
            }
            return query;
        }

        /**
         * builds a queryString when user submit advancedSearch form
         * @return rsql query string
         */
        function buildAdvanceQuery() {
            var query = QueryCache.getSpectraQuery();
            var compiled = '';

            // compound name, inchiKey and class
            if(angular.isDefined(query.compound) && query.compound.length !== 0) {
                compiled = qStrHelper.buildCompoundString(query.compound);
            }

            if(angular.isDefined(query.compoundMetada && query.compoundMetada.length > 0)) {
                var test = qStrHelper.buildMetaString(query.compoundMetada, true);
                $log.info(test);
            }

            $log.info(query);
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
