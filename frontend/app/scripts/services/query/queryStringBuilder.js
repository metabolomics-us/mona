/**
 * This factory will parse the query Modal object and returns an RSQL Query String
 *
 */

(function () {
    'use strict';
    angular.module('moaClientApp')
        .factory("queryStringBuilder", queryStringBuilder);

    /* @ngInject */
    function queryStringBuilder($log, QueryCache) {

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

            // build compound string
            if (angular.isDefined(query.compound) && query.compound.length !== 0) {
                compoundQuery = compoundQuery.concat(addCompoundQueryString(query.compound));
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

            // build metadata from on fly updates
            if (angular.isDefined(query.metadata) && query.metadata.length !== 0) {
                metadataQuery += addMetaDataQueryString(query.metadata);
            }

            // compile compound & meta queries
            var compiledQuery = compoundQuery === '' && metadataQuery === '' ? '/rest/spectra' :
                compoundQuery === '' ? metadataQuery :
                    metadataQuery === '' ? compoundQuery :
                        compoundQuery.concat(' and ',metadataQuery);

            // set query in cache
            QueryCache.setSpectraQueryString(compiledQuery);

        }

        // handles each meta group for keyword filter
        function addMetaFilterQueryString(filterOptions) {
            var filtered = [];
            for (var key in filterOptions) {
                if(filterOptions.hasOwnProperty(key) && filterOptions[key].length !== 0) {
                    filtered.push(addGroupMetaQueryString(key, filterOptions[key]));
                }
            }

            return filtered.length === 0 ? '' : filtered.length > 1 ? filtered.join(' and ') : filtered.join('');
        }

        // builds and encapsulate each group with ()
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

        function addCompoundQueryString(compound) {
            var query = '';

            for (var i = 0; i < compound.length; i++) {
                var curCompound = compound[i];

                if (query !== '') {
                    query += ' or ';
                }

                for (var key in curCompound) {
                    if (key === 'name') {
                        query += "compound.names=q='name==" + '\"' + curCompound[key] + '\"\'';
                    }
                    else if (key === 'inchiKey') {
                        query += "compound.inchiKey==" + curCompound[key] + "\"";
                    }
                    else {
                        query += "compound.classification=q='value==" + '\"' + curCompound[key] + '\"\'';
                    }
                }
            }

            return query;
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

        function addMetaDataQueryString(metadata) {
            var query = '';

            for (var i = 0, l = metadata.length; i < l; i++) {
                var meta = metadata[i];

                if (query !== '') {
                    query += ' or ';
                }

                for (var key in meta) {
                    query +="metaData=q='name==\"" + key + "\" and value==\"" + meta[key] + "\"'";
                }
            }

            return query;
        }


    }
})();
