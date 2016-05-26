/**
 * This factory will parse the query Modal object and returns an RSQL Query String
 *
 */

(function () {
    'use strict';
    angular.module('moaClientApp')
        .factory("rsqlService", rsqlService);

    /* @ngInject */
    function rsqlService($log, QueryCache) {

        var service = {

        };
        return service;


        /**
         * parses a query object and returns a RSQL Query String
         * @param query
         * @return rsql query string
         */

        function buildRsqlQuery(options) {
            var filtered = options;
            var compoundQuery = '';
            var metadataQuery = '';


            // build compound string
            if (typeof(filtered.compound) === 'object' && (JSON.stringify(filtered.compound) !== JSON.stringify({}))) {
                compoundQuery = compoundQuery.concat(buildCompoundQueryString(filtered.compound));
            }

            // build metadata string
            if (typeof(filtered.metadata) === 'object' && JSON.stringify(filtered.metadata) !== JSON.stringify({})) {
                metadataQuery = buildMetaDataQueryString(filtered.metadata);
            }

            //TODO build tag string

            var compiledQuery = '';

            // strip leading 'and' if compoundQuery is empty
            compiledQuery = compoundQuery === '' ? metadataQuery.slice(5) :
                compiledQuery.concat(compoundQuery, metadataQuery);

            // set query in cache
            compiledQuery = compiledQuery === '' ? '/rest/spectra' : compiledQuery;
            setRsqlQuery(compiledQuery);
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

            // handle exact mass & tolerance
            if (typeof(metadata.exactMass) !== 'undefined') {
                // concat first operand
                query = query.concat(' ', getQuery().firstOperand.toLowerCase());
                var leftOffset = metadata.exactMass - metadata.tolerance;
                var rightOffset = metadata.exactMass + metadata.tolerance;
                query += " metaData=q='name==\"exact mass\" and " + "(value>=\"" + leftOffset + "\" or value<=\"" + rightOffset + "\")'";
            }

            // handle formula
            if (typeof(metadata.formula) !== 'undefined' && metadata.formula !== '') {
                var secondOperand = getQuery().secondOperand.toLowerCase();
                query += ' ' + secondOperand + ' ' + "metaData=q='name==\"formula\" and value==\"" + metadata.formula + "\"'";
            }


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

            // handle compound name
            if (typeof(compound.name) !== 'undefined') {
                query += "compound.names=q='name==" + '\"' + compound.name + '\"\'';

            }

            // handle compound inchiKey
            else if (typeof(compound.inchiKey) !== 'undefined') {
                query += "compound.inchiKey==" + '\"' + compound.inchiKey + '\"\'';

            }

            return query;
        }

    }
})();
