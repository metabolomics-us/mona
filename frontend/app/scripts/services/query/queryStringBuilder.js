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
                compoundQuery = compoundQuery.concat(buildCompoundQueryString(query.compound));
            }

            // build compound metadata string
            if (angular.isDefined(query.compoundMeta) && query.compoundMeta.length !== 0) {
                var compoundMetaQuery = buildCompoundMetaQueryString(query.compoundMeta, query.operand);
                compoundQuery = compoundQuery === '' ? compoundMetaQuery : compoundQuery.concat(compoundMetaQuery);
            }

            //build metadata filter string from search page

            if(angular.isDefined(query.metaFilter)) {
                var metaFilterQuery = buildMetaFilterQueryString(query.metaFilter);
            }

            // build metadata string
            if (angular.isDefined(query.metadata) && query.metadata.length !== 0) {
                metadataQuery += buildMetaDataQueryString(query.metadata);
            }

            //TODO build tag string

            var compiledQuery = '';

            //$log.debug('compound: ' + compoundQuery);
            //$log.debug('metadata: ' + metadataQuery);
            //$log.debug(metadataQuery.substring(1,4) === 'and');
            //$log.debug(metadataQuery.substring(1,3) === 'or');


            compiledQuery = (metadataQuery.substring(1,4) === 'and' || metadataQuery.substring(1,3) === 'or') ?
                compiledQuery.concat(compoundQuery, metadataQuery) :
                    compoundQuery === '' ? metadataQuery :
                        metadataQuery === '' ? compoundQuery :
                            compiledQuery.concat(compoundQuery, ' and ', metadataQuery);

            //$log.info(compiledQuery);

            // set query in cache
            compiledQuery = compiledQuery === '' ? '/rest/spectra' : compiledQuery;
            QueryCache.setSpectraQueryString(compiledQuery);

        }

        function buildMetaFilterQueryString(filterOptions) {
            var query = '';
            

            return query;
        }

        function buildCompoundMetaQueryString(metadata, operand) {
            var query = '';

            // handle exact mass & tolerance
            for (var i = 0; i < metadata.length; i ++) {

                if (metadata[i].hasOwnProperty('exact mass')) {
                    // concat first operand
                    query = query.concat(' ', operand[0]);
                    var leftOffset = metadata[i]['exact mass'] - metadata[i+1].tolerance;
                    var rightOffset = metadata[i]['exact mass'] + metadata[i+1].tolerance;
                    query += " compound.metaData=q='name==\"exact mass\" and " + "value>=\"" + leftOffset + "\" or value<=\"" + rightOffset + "\"'";
                }

                // handle formula
                if (metadata[i].hasOwnProperty('formula')) {
                    var secondOperand = operand[1];
                    query += ' ' + secondOperand + ' ' + "compound.metaData=q='name==\"formula\" and value==\"" + metadata[i].formula + "\"'";
                }
            }
            return query;
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

        function buildMetaDataQueryString(metadata) {
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

        function buildCompoundQueryString(compound) {
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

    }
})();
