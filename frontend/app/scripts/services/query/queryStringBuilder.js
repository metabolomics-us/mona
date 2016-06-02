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

        var service = {
            buildQueryString: buildQueryString
        };
        return service;

        /**
         * parses a query object and returns a RSQL Query String
         * @param query
         * @return rsql query string
         */
        function buildQueryString() {
            var query = QueryCache.getSpectraQuery();
            var compoundQuery = '';
            var metadataQuery = '';

            qStrHelper.buildMetaString([{name: 'test1', value: 'test1_value'},
                {name: 'test2', value: 'test2_value', operator: 'ne'},
                {name: 'test3'}]);
            // build compound string
            if (angular.isDefined(query.compound) && query.compound.length !== 0) {
                compoundQuery = compoundQuery.concat(qStrHelper.buildCompoundString(query.compound));
            }

            // build compound metadata string
            if (angular.isDefined(query.compoundDa) && query.compoundDa.length !== 0) {
                var compoundMetaQuery = addMeasurementQueryString(query.compoundDa, query.operand);

                // strip leading operators
                compoundQuery = compoundQuery === '' && compoundMetaQuery.substring(1,4) === 'and' ? compoundMetaQuery.slice(5) :
                    compoundQuery === '' && compoundMetaQuery.substring(1,3) === 'or' ? compoundMetaQuery.slice(4) :
                        compoundQuery.concat(compoundMetaQuery);
            }

            //build metadata filter string from search page
            if(angular.isDefined(query.metaFilter)) {
                metadataQuery += addMetaFilterQueryString(query.metaFilter);
            }

            // compile compound & meta queries
            var compiledQuery = compoundQuery === '' && metadataQuery === '' ? '/rest/spectra' :
                compoundQuery === '' ? metadataQuery :
                    metadataQuery === '' ? compoundQuery :
                        compoundQuery.concat(' and ',metadataQuery);

            // set query in cache
            QueryCache.setSpectraQueryString(compiledQuery);

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

        // HELPER method that builds and encapsulate each meta group with ()
        function addGroupMetaQueryString(key, arr) {
            var query = '';

            for(var i = 0, l = arr.length; i < l; i++) {
                if (query !== '') {
                    query += ' or ';
                }

                query += "metaData=q='name==\"" + key + "\" and value==\"" + arr[i] + "\"'";
            }
            return '('.concat(query,')');
        }


        // build querystring for mass tolerance and formula
        function addMeasurementQueryString(measurement, operand) {
            var query = '';

            // handle exact mass & tolerance
            for (var i = 0; i < measurement.length; i ++) {

                if (measurement[i].hasOwnProperty('exact mass')) {
                    // concat first operand
                    query = query.concat(' ', operand[0]);
                    var leftOffset = measurement[i]['exact mass'] - measurement[i+1].tolerance;
                    var rightOffset = measurement[i]['exact mass'] + measurement[i+1].tolerance;
                    query += " compound.metaData=q='name==\"exact mass\" and " + "value>=\"" + leftOffset + "\" or value<=\"" + rightOffset + "\"'";
                }

                // handle formula
                if (measurement[i].hasOwnProperty('formula')) {
                    var secondOperand = operand[1];
                    query += ' ' + secondOperand + ' ' + "compound.metaData=q='name==\"formula\" and value==\"" + measurement[i].formula + "\"'";
                }
            }
            return query;
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
