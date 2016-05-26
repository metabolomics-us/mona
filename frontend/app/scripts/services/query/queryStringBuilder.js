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
            $log.info(query);
            var compoundQuery = '';
            var metadataQuery = '';


            // build compound string
            if (angular.isDefined(query.compound) && query.compound.length !== 0) {
                compoundQuery = compoundQuery.concat(buildCompoundQueryString(query.compound));
            }

            // build metadata string
            if (angular.isDefined(query.metadata) && query.metadata.length !== 0) {
                metadataQuery = buildMetaDataQueryString(query.metadata, query.operand);
            }

            //TODO build tag string

            var compiledQuery = '';

            // strip leading 'and' if compoundQuery is empty
            compiledQuery = compoundQuery === '' ? metadataQuery.slice(5) :
                compiledQuery.concat(compoundQuery, metadataQuery);

            // set query in cache
            compiledQuery = compiledQuery === '' ? '/rest/spectra' : compiledQuery;

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

        function buildMetaDataQueryString(metadata, operand) {
            var query = '';
            $log.debug(metadata);
            // handle exact mass & tolerance
            for (var i = 0; i < 2; i ++) {

                if (metadata[i].hasOwnProperty('exact mass')) {
                    // concat first operand
                    query = query.concat(' ', operand.shift());
                    var leftOffset = metadata[i]['exact mass'] - metadata[i+1].tolerance;
                    var rightOffset = metadata[i]['exact mass'] + metadata[i+1].tolerance;
                    query += " metaData=q='name==\"exact mass\" and " + "(value>=\"" + leftOffset + "\" or value<=\"" + rightOffset + "\")'";
                }

                // handle formula
                if (metadata[i].hasOwnProperty('formula')) {
                    var secondOperand = operand.pop();
                    query += ' ' + secondOperand + ' ' + "metaData=q='name==\"formula\" and value==\"" + metadata[i].formula + "\"'";
                }
            }
            $log.info(query);

            // handle instrument Type
            if (typeof(metadata.insType) !== 'undefined' && metadata.insType.length !== 0) {
                for (var i = 0, l = metadata.insType.length; i < l; i++) {
                    query += " and metaData=q='name==\"instrument type\" and value==\"" + metadata.insType[i] + "\"'";
                }
            }

            // handle msType
            if (typeof(metadata.msType) !== 'undefined' && metadata.msType.length !== 0) {
                for (var i = 0, l = metadata.msType.length; i < l; i++) {
                    query += " and metaData=q='name==\"ms type\" and value==\"" + metadata.msType[i] + "\"'";
                }
            }

            // handle ionMode
            if (typeof(metadata.ionMode) !== 'undefined' && metadata.ionMode.length !== 0) {
                for (var i = 0; i < metadata.ionMode.length; i++) {
                    query += " and metaData=q='name==\"ion mode\" and value==\"" + metadata.ionMode[i] + "\"'";
                }
            }
            return query;
        }

        function buildCompoundQueryString(compound) {
            var query = '';

            for (var i = 0; i < compound.length; i++) {
                var curCompound = compound[i];

                if (query !== '') {
                    query += ' and ';
                }

                for (var key in curCompound) {
                    if (key === 'name') {
                        query += "compound.names=q='name==" + '\"' + curCompound[key] + '\"\'';
                    }
                    else {
                        query += "compound." + key + "==\"" + curCompound[key] + "\"";
                    }
                }
            }

            return query;
        }

    }
})();
